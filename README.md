# CoronaStatsDE
[![](https://jitpack.io/v/com.gitlab.asdoi/coronastats.svg)](https://jitpack.io/#com.gitlab.asdoi/coronastats)
[![License](https://img.shields.io/badge/dynamic/json.svg?label=License&url=https://gitlab.com/api/v4/projects/22258695?license=true&query=license.key&colorB=yellow)]()
[![Stars](https://img.shields.io/badge/dynamic/json.svg?style=social&label=Stars&url=https://gitlab.com/api/v4/projects/22258695&query=star_count&logo=gitlab)]()  

This app displays the current corona data, e.g. total infections, deaths, etc., of the respective areas in Germany.  
In addition, the colors of the corona traffic lights are displayed in order to have clarity at a glance about possible restrictions (7-day incidence per 100,000 population <35: green ; <50: yellow ; >50: red ; In Bavaria >100: dark red).  
Only the current data is displayed, no histories over several days.

### The following data sources are used:
 - For <a href="https://www.lgl.bayern.de/gesundheit/infektionsschutz/infektionskrankheiten_a_z/coronavirus/karte_coronavirus/index.htm">Bavarian cities and districts</a> the data of the <a href="https://www.lgl.bayern.de/">LGL Bayern</a>
  - For all other <a href="https://npgeo-corona-npgeo-de.hub.arcgis.com/datasets/917fc37a709542548cc3be077a786c17_0">cities, counties</a>, <a href="https://npgeo-corona-npgeo-de.hub.arcgis.com/datasets/ef4b445a53c1406892257fe63129a8ea_0">states</a> and <a href="https://www.rki.de/DE/Content/InfAZ/N/Neuartiges_Coronavirus/Fallzahlen.html">Germany</a> the data of the <a href="https://www.rki.de/">Robert Koch Institute</a>
   - For worldwide statistics the data from <a href="https://www.worldometers.info/">worldometers.info</a>

### Library:
The parsing of the data was extracted to a separate library, which can also parse the data from the <a href="https://www.jhu.edu/">Johns Hopkins University</a> and additional data from <a href="https://www.worldometers.info/">worldometers.info</a>.  
Of course, it may also be used in other projects.

Add it in your root build.gradle at the end of repositories:
```
   allprojects {
      repositories {
         ...
         maven { url 'https://jitpack.io' }
      }
   }
```

Add the dependency
```
   dependencies {
      implementation 'com.gitlab.asdoi:coronastats:v1.1'
   }
```


### Screenshots:
<img src="https://gitlab.com/asdoi/coronastatsde-beta/-/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/Screen1.png?inline=false" width="25%">
<img src="https://gitlab.com/asdoi/coronastatsde-beta/-/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/Screen2.png?inline=false" width="25%">
<img src="https://gitlab.com/asdoi/coronastatsde-beta/-/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/Screen3.png?inline=false" width="25%">
