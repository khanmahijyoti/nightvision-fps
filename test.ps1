param([switch]$WithArena,[switch]$Gallery)
$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$mc = Join-Path $env:APPDATA '.minecraft'
$jdk = Join-Path $mc 'runtime\java-runtime-epsilon\windows\java-runtime-epsilon\bin'
$version = Get-Content -Raw "$mc\versions\26.3\26.3.json" | ConvertFrom-Json
$jars = @("$mc\versions\26.3\26.3.jar")
foreach ($library in $version.libraries) {
    if ($library.downloads.artifact.path) {
        $path = Join-Path "$mc\libraries" $library.downloads.artifact.path
        if (Test-Path -LiteralPath $path) { $jars += $path }
    }
}
$jars += Get-ChildItem -LiteralPath "$project\deps" -Filter *.jar | Select-Object -ExpandProperty FullName
$classpath = ($jars + "$project\build\classes") -join ';'
New-Item -ItemType Directory -Path "$project\build\test","$project\test-instance\mods","$project\test-instance\saves" -Force | Out-Null
$testSources = @(Get-ChildItem -LiteralPath "$project\test\sidearm" -Filter *.java | Select-Object -ExpandProperty FullName)
& "$jdk\javac.exe" -proc:none --release 25 -classpath $classpath -d "$project\build\test" @testSources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
'{"schemaVersion":1,"id":"sidearm_smoke_test","version":"1.0.0","environment":"client","mixins":["sidearm-smoke.mixins.json"]}' | Set-Content "$project\build\test\fabric.mod.json"
$testClass = if ($Gallery) { 'GalleryCapture' } else { 'PistolSmokeTest' }
('{"required":true,"package":"sidearm.smokemixin","compatibilityLevel":"JAVA_25","client":["'+$testClass+'"],"injectors":{"defaultRequire":1}}') | Set-Content "$project\build\test\sidearm-smoke.mixins.json"
& "$jdk\jar.exe" --create --file "$project\test-instance\mods\arena-smoke-test.jar" -C "$project\build\test" .
if ($LASTEXITCODE -ne 0) { throw 'Test packaging failed' }
Copy-Item -LiteralPath "$project\dist\nightvision-fps-0.1.0-alpha.3.jar" -Destination "$project\test-instance\mods" -Force
Get-ChildItem -LiteralPath "$project\test-instance\mods" -File | Where-Object { $_.Name -like 'nightvision-fps-*.jar' -or $_.Name -like 'iron-sidearm-*.jar' } |
    Where-Object { $_.Name -ne 'nightvision-fps-0.1.0-alpha.3.jar' } |
    Remove-Item
Copy-Item -LiteralPath "$project\deps\fabric-api.jar" -Destination "$project\test-instance\mods\fabric-api.jar" -Force
$companion = "$project\test-instance\mods\ashen-arena-1.0.0.jar"
if ($WithArena) {
    Copy-Item -LiteralPath 'E:\MinecraftArena\dist\ashen-arena-1.0.0.jar' -Destination $companion -Force
} elseif (Test-Path -LiteralPath $companion) {
    Remove-Item -LiteralPath $companion
}
$testWorld = "$project\test-instance\saves\ArenaTest"
if (!(Test-Path -LiteralPath $testWorld)) {
    Copy-Item -LiteralPath "$mc\saves\New World" -Destination $testWorld -Recurse
}
$optionsPath = "$project\test-instance\options.txt"
$options = @('renderDistance:8','simulationDistance:5','pauseOnLostFocus:false','onboardAccessibility:false','tutorialStep:none','maxFps:60')
if (Test-Path -LiteralPath $optionsPath) {
    $options += Get-Content -LiteralPath $optionsPath | Where-Object { $_ -notmatch '^(renderDistance|simulationDistance|pauseOnLostFocus|onboardAccessibility|tutorialStep|maxFps):' }
}
$options | Set-Content -LiteralPath $optionsPath
$launchArgs = @('-Xmx3G','-XX:StackShadowPages=32','--enable-native-access=ALL-UNNAMED','--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED','-XX:+UseZGC','-XX:+AlwaysPreTouch','-XX:+UseCompactObjectHeaders','-cp',($jars -join ';'),'net.fabricmc.loader.impl.launch.knot.KnotClient','--username','SidearmTest','--version','26.3','--gameDir',"$project\test-instance",'--assetsDir',"$mc\assets",'--assetIndex',$version.assetIndex.id,'--uuid','00000000000000000000000000000001','--accessToken','0','--userType','msa','--versionType','release','--quickPlaySingleplayer','ArenaTest','--width','1280','--height','720')
if ($WithArena) { $launchArgs = @('-Dsidearm.test.withArena=true','-Dashen_arena.disableBoss=true') + $launchArgs }
$argumentLines = $launchArgs | ForEach-Object { '"' + $_.Replace('\','\\') + '"' }
[IO.File]::WriteAllLines("$project\test-args.txt",$argumentLines,[Text.UTF8Encoding]::new($false))
Push-Location "$project\test-instance"
try {
    $started = Get-Date
    & "$jdk\java.exe" "@$project\test-args.txt"
    if ($LASTEXITCODE -ne 0) { throw "Test client exited with code $LASTEXITCODE" }
    $resultPath = if ($Gallery) { "$project\gallery-result.txt" } else { "$project\pistol-test-result.txt" }
    $result = Get-Item -LiteralPath $resultPath
    $text = Get-Content -Raw -LiteralPath $result.FullName
    if ($result.LastWriteTime -lt $started -or !$text.StartsWith('PASS:')) { throw "No passing result from this run: $text" }
    Write-Output $text
} finally { Pop-Location }
