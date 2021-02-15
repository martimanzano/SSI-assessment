# SSI-assessment
Component that uses Bayesian Networks to estimate the assessment for software strategic indicators

This component has been partially created as a result of the Q-Rapids project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732253.

## Main Functionality
This library uses Bayesian Networks for computing Software Strategic Indicators.

## Technologies
|Property|Description|
| -------------------- | ---------|
| Type of component    | Library  |
| Build                | .jar     |
| Programming language | Java     |
| Frameworks           | Gradle   |
| External libraries   | UnBBayes |

## How to build
This is a Gradle project. You can use any IDE that supports Gradle to build it, or alternatively you can use the command line using the Gradle wrapper with the command *__gradlew__* if you don't have Gradle installed on your machine or with the command *__gradle__* if you do, followed by the name of the task. If you want to build this library without dependencies, use the task *__jar__*, and if you want the dependencies being included in the package, use the task *__customFatJar__*.

```
# Example: using Gradle wrapper to build with dependencies
cd SSI_assessment
gradlew customFatJar
```
After the build is done the JAR file can be found at the __build/libs__ directory

## Documentation
You can find the technical documentation of the API [here](https://q-rapids.github.io/qrapids-si_assessment).

## Contributing
You can find guidelines to contribute to the project in the [Q-Rapids repository](https://github.com/q-rapids/q-rapids/blob/master/CONTRIBUTING.md).

## Licensing
This program is free software: you can redistribute it and/or modify 	it under the terms of the GNU General Public License as published by 	the Free Software Foundation, either version 3 of the License, or 	 (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see [https://www.gnu.org/licenses/](https://www.gnu.org/licenses/).


## Contact
For problems regarding this component, please open an issue in the [issues section](https://github.com/martimanzano/SSI-assessment/issues).
