$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$mc = Join-Path $env:APPDATA '.minecraft'
$jdk = Join-Path $mc 'runtime\java-runtime-epsilon\windows\java-runtime-epsilon\bin'
$version = Get-Content -Raw -LiteralPath "$mc\versions\26.3\26.3.json" | ConvertFrom-Json
$jars = @("$mc\versions\26.3\26.3.jar")
foreach ($lib in $version.libraries) {
    if ($lib.downloads.artifact.path) {
        $path = Join-Path "$mc\libraries" $lib.downloads.artifact.path
        if (Test-Path -LiteralPath $path) { $jars += $path }
    }
}
$jars += Get-ChildItem -LiteralPath "$project\deps" -Filter *.jar | Select-Object -ExpandProperty FullName
$cp = $jars -join ';'
$classes = [IO.Path]::GetFullPath((Join-Path $project 'build\classes'))
$workspace = [IO.Path]::GetFullPath($project).TrimEnd('\') + '\'
if (!$classes.StartsWith($workspace,[StringComparison]::OrdinalIgnoreCase) -or $classes -ne (Join-Path $project 'build\classes')) { throw 'Unsafe classes path' }
if (Test-Path -LiteralPath $classes) { Remove-Item -LiteralPath $classes -Recurse -Force }
New-Item -ItemType Directory -Path $classes,"$project\dist" -Force | Out-Null
$sources = Get-ChildItem -LiteralPath "$project\src" -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
& "$jdk\javac.exe" -proc:none --release 25 -classpath $cp -d "$project\build\classes" @sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
Copy-Item -Path "$project\resources\*" -Destination "$project\build\classes" -Recurse -Force
Copy-Item -LiteralPath "$project\LICENSE" -Destination "$project\build\classes\LICENSE"
& "$jdk\jar.exe" --create --file "$project\dist\nightvision-fps-0.1.0-alpha.3.jar" -C "$project\build\classes" .
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed' }
Write-Output "Built $project\dist\nightvision-fps-0.1.0-alpha.3.jar"

