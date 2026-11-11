# Development dependencies

Download these files into this directory before running `build.ps1`. Use the filenames in the first column. The build loads JARs from this directory, and `test.ps1` expects `fabric-api.jar` by name.

| Local filename | Version | Download |
| --- | --- | --- |
| fabric-loader.jar | 0.20.1 | [Fabric Maven](https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.20.1/fabric-loader-0.20.1.jar) |
| sponge-mixin.jar | 0.17.5+mixin.0.8.7 | [Fabric Maven](https://maven.fabricmc.net/net/fabricmc/sponge-mixin/0.17.5+mixin.0.8.7/sponge-mixin-0.17.5+mixin.0.8.7.jar) |
| asm.jar | 9.10.1 | [Fabric Maven](https://maven.fabricmc.net/org/ow2/asm/asm/9.10.1/asm-9.10.1.jar) |
| asm-analysis.jar | 9.10.1 | [Fabric Maven](https://maven.fabricmc.net/org/ow2/asm/asm-analysis/9.10.1/asm-analysis-9.10.1.jar) |
| asm-commons.jar | 9.10.1 | [Fabric Maven](https://maven.fabricmc.net/org/ow2/asm/asm-commons/9.10.1/asm-commons-9.10.1.jar) |
| asm-tree.jar | 9.10.1 | [Fabric Maven](https://maven.fabricmc.net/org/ow2/asm/asm-tree/9.10.1/asm-tree-9.10.1.jar) |
| asm-util.jar | 9.10.1 | [Fabric Maven](https://maven.fabricmc.net/org/ow2/asm/asm-util/9.10.1/asm-util-9.10.1.jar) |
| fabric-api.jar | 0.166.0+26.3 | [Modrinth](https://modrinth.com/mod/fabric-api/version/998R2rNP) |

The Minecraft client and its libraries are read from the local Minecraft installation by the build script. Third-party dependencies retain their respective licenses.
