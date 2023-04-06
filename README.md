# HARMOLOID2

[![Discord](https://img.shields.io/discord/984044285584359444?style=for-the-badge&label=discord&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/TyEcQ6P73y)

HARMOLOID is an application for generating simple chorus based on projects of singing voice synthesizers.

The current version `2.x` is built with [Kotlin for JavaScript](https://kotlinlang.org/docs/js-overview.html)
and [React](https://github.com/facebook/react).

## Supported formats

- VOCALOID 3/4 project (.vsqx)
- VOCALOID 5 project (.vpr)
- UTAU project (.ust)
- CeVIO project (.ccs)
- Synthesizer V Studio project (.svp)
- OpenUtau project (.ustx)
- [UtaFormatix Data](https://github.com/sdercolin/utaformatix-data) (.ufdata)

## Project structure

This repository only contains source code of the HARMOLOID2 application. The algorithms are extracted as a library.
Check details at: [harmoloid-core-kt](https://github.com/sdercolin/harmoloid-core-kt)

## Contribution

This project is basically built in the same structure with [utaformatix3](https://github.com/sdercolin/utaformatix3),
sharing some code written by other developers. Thank you very much for your effort.

## Get started for development

1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/)
2. Clone and import as a Gradle project
3. Run by `./gradlew run`

## License

[Apache License, Version 2.0](https://github.com/sdercolin/harmoloid2/blob/main/LICENSE.md)
