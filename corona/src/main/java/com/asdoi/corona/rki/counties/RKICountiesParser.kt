package com.asdoi.corona.rki.counties

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object RKICountiesParser : LiveTickerParser() {
    private fun getAPIUrl(city: String): String {
        return "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?where=GEN%20%3D%20%27$city%27&outFields=*&outSR=4326&f=json"
    }

    private fun parseLiveTickers(vararg documents: Document): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()

        for (document in documents) {
            try {
                val json = JSONObject(document.text())
                val featuresArray = json.getJSONArray("features").getJSONObject(0)
                val attributes = featuresArray.getJSONObject("attributes")

                val location = attributes.getString("GEN")
                try {
                    val cases = attributes.getInt("cases")
                    val deaths = attributes.getInt("deaths")
                    val casesPerOneHundredThousands = attributes.getDouble("cases_per_100k")
                    val sevenDayIncidencePerOneHundredThousands =
                        attributes.getDouble("cases7_per_100k")
                    val deathRate = attributes.getDouble("death_rate")
                    val infectionsPerPopulation = attributes.getDouble("cases_per_population")

                    val lastUpdateString = attributes.getString("last_update").replace("Uhr", "")
                    val lastUpdate: Calendar =
                        try {
                            val dateFormat = SimpleDateFormat("dd.MM.yyy, mm:HH", Locale.GERMANY)

                            val calendar = Calendar.getInstance()
                            calendar.time = dateFormat.parse(lastUpdateString)!!
                            calendar
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Calendar.getInstance()
                        }

                    tickers.add(
                        RKICountyTicker(
                            location,
                            lastUpdate,
                            cases,
                            deaths,
                            casesPerOneHundredThousands,
                            sevenDayIncidencePerOneHundredThousands,
                            deathRate,
                            infectionsPerPopulation
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    tickers.add(
                        ParseError(
                            location,
                            RKICountyTicker.DATA_SOURCE,
                            RKICountyTicker.VISIBLE_DATA_SOURCE,
                            e
                        )
                    )
                }
            } catch (ignore: Exception) {
            }
        }

        return tickers
    }

    fun parse(vararg documents: Document): List<LiveTicker> {
        return parseLiveTickers(*documents)
    }

    fun parseNoErrors(vararg documents: Document) = parse(*documents).filter { !it.isError() }

    fun parseNoInternalErrors(vararg documents: Document) = parse(*documents).filter {
        if (it.isError()) {
            !(it as ParseError).isInternalError()
        } else
            true
    }

    @Throws(IOException::class)
    fun downloadDocuments(vararg cities: String): List<Document> {
        val documents: MutableList<Document> = mutableListOf()
        for (city in cities) {
            val document = Jsoup.connect(getAPIUrl(city)).ignoreContentType(true).get();

            if (document != null)
                documents.add(document)
            else
                throw IOException()
        }
        return documents
    }

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return parse(*downloadDocuments(*locations).toTypedArray())
    }

    override fun getSuggestions(): List<String> {
        return listOf(
            "Flensburg",
            "Kiel",
            "Lübeck",
            "Neumünster",
            "Dithmarschen",
            "Herzogtum Lauenburg",
            "Nordfriesland",
            "Ostholstein",
            "Pinneberg",
            "Plön",
            "Rendsburg-Eckernförde",
            "Schleswig-Flensburg",
            "Segeberg",
            "Steinburg",
            "Stormarn",
            "Hamburg",
            "Braunschweig",
            "Salzgitter",
            "Wolfsburg",
            "Gifhorn",
            "Goslar",
            "Helmstedt",
            "Northeim",
            "Peine",
            "Wolfenbüttel",
            "Göttingen",
            "Region Hannover",
            "Diepholz",
            "Hameln-Pyrmont",
            "Hildesheim",
            "Holzminden",
            "Nienburg (Weser)",
            "Schaumburg",
            "Celle",
            "Cuxhaven",
            "Harburg",
            "Lüchow-Dannenberg",
            "Lüneburg",
            "Osterholz",
            "Rotenburg (Wümme)",
            "Heidekreis",
            "Stade",
            "Uelzen",
            "Verden",
            "Delmenhorst",
            "Emden",
            "Oldenburg (Oldb)",
            "Osnabrück",
            "Wilhelmshaven",
            "Ammerland",
            "Aurich",
            "Cloppenburg",
            "Emsland",
            "Friesland",
            "Grafschaft Bentheim",
            "Leer",
            "Oldenburg",
            "Osnabrück",
            "Vechta",
            "Wesermarsch",
            "Wittmund",
            "Bremen",
            "Bremerhaven",
            "Düsseldorf",
            "Duisburg",
            "Essen",
            "Krefeld",
            "Mönchengladbach",
            "Mülheim an der Ruhr",
            "Oberhausen",
            "Remscheid",
            "Solingen",
            "Wuppertal",
            "Kleve",
            "Mettmann",
            "Rhein-Kreis Neuss",
            "Viersen",
            "Wesel",
            "Bonn",
            "Köln",
            "Leverkusen",
            "Städteregion Aachen",
            "Düren",
            "Rhein-Erft-Kreis",
            "Euskirchen",
            "Heinsberg",
            "Oberbergischer Kreis",
            "Rheinisch-Bergischer Kreis",
            "Rhein-Sieg-Kreis",
            "Bottrop",
            "Gelsenkirchen",
            "Münster",
            "Borken",
            "Coesfeld",
            "Recklinghausen",
            "Steinfurt",
            "Warendorf",
            "Bielefeld",
            "Gütersloh",
            "Herford",
            "Höxter",
            "Lippe",
            "Minden-Lübbecke",
            "Paderborn",
            "Bochum",
            "Dortmund",
            "Hagen",
            "Hamm",
            "Herne",
            "Ennepe-Ruhr-Kreis",
            "Hochsauerlandkreis",
            "Märkischer Kreis",
            "Olpe",
            "Siegen-Wittgenstein",
            "Soest",
            "Unna",
            "Darmstadt",
            "Frankfurt am Main",
            "Offenbach am Main",
            "Wiesbaden",
            "Bergstraße",
            "Darmstadt-Dieburg",
            "Groß-Gerau",
            "Hochtaunuskreis",
            "Main-Kinzig-Kreis",
            "Main-Taunus-Kreis",
            "Odenwaldkreis",
            "Offenbach",
            "Rheingau-Taunus-Kreis",
            "Wetteraukreis",
            "Gießen",
            "Lahn-Dill-Kreis",
            "Limburg-Weilburg",
            "Marburg-Biedenkopf",
            "Vogelsbergkreis",
            "Kassel",
            "Fulda",
            "Hersfeld-Rotenburg",
            "Kassel",
            "Schwalm-Eder-Kreis",
            "Waldeck-Frankenberg",
            "Werra-Meißner-Kreis",
            "Koblenz",
            "Ahrweiler",
            "Altenkirchen (Westerwald)",
            "Bad Kreuznach",
            "Birkenfeld",
            "Cochem-Zell",
            "Mayen-Koblenz",
            "Neuwied",
            "Rhein-Hunsrück-Kreis",
            "Rhein-Lahn-Kreis",
            "Westerwaldkreis",
            "Trier",
            "Bernkastel-Wittlich",
            "Eifelkreis Bitburg-Prüm",
            "Vulkaneifel",
            "Trier-Saarburg",
            "Frankenthal (Pfalz)",
            "Kaiserslautern",
            "Landau in der Pfalz",
            "Ludwigshafen am Rhein",
            "Mainz",
            "Neustadt an der Weinstraße",
            "Pirmasens",
            "Speyer",
            "Worms",
            "Zweibrücken",
            "Alzey-Worms",
            "Bad Dürkheim",
            "Donnersbergkreis",
            "Germersheim",
            "Kaiserslautern",
            "Kusel",
            "Südliche Weinstraße",
            "Rhein-Pfalz-Kreis",
            "Mainz-Bingen",
            "Südwestpfalz",
            "Stuttgart",
            "Böblingen",
            "Esslingen",
            "Göppingen",
            "Ludwigsburg",
            "Rems-Murr-Kreis",
            "Heilbronn",
            "Heilbronn",
            "Hohenlohekreis",
            "Schwäbisch Hall",
            "Main-Tauber-Kreis",
            "Heidenheim",
            "Ostalbkreis",
            "Baden-Baden",
            "Karlsruhe",
            "Karlsruhe",
            "Rastatt",
            "Heidelberg",
            "Mannheim",
            "Neckar-Odenwald-Kreis",
            "Rhein-Neckar-Kreis",
            "Pforzheim",
            "Calw",
            "Enzkreis",
            "Freudenstadt",
            "Freiburg im Breisgau",
            "Breisgau-Hochschwarzwald",
            "Emmendingen",
            "Ortenaukreis",
            "Rottweil",
            "Schwarzwald-Baar-Kreis",
            "Tuttlingen",
            "Konstanz",
            "Lörrach",
            "Waldshut",
            "Reutlingen",
            "Tübingen",
            "Zollernalbkreis",
            "Ulm",
            "Alb-Donau-Kreis",
            "Biberach",
            "Bodenseekreis",
            "Ravensburg",
            "Sigmaringen",
            "Ingolstadt",
            "München",
            "Rosenheim",
            "Altötting",
            "Berchtesgadener Land",
            "Bad Tölz-Wolfratshausen",
            "Dachau",
            "Ebersberg",
            "Eichstätt",
            "Erding",
            "Freising",
            "Fürstenfeldbruck",
            "Garmisch-Partenkirchen",
            "Landsberg am Lech",
            "Miesbach",
            "Mühldorf a. Inn",
            "München",
            "Neuburg-Schrobenhausen",
            "Pfaffenhofen a.d. Ilm",
            "Rosenheim",
            "Starnberg",
            "Traunstein",
            "Weilheim-Schongau",
            "Landshut",
            "Passau",
            "Straubing",
            "Deggendorf",
            "Freyung-Grafenau",
            "Kelheim",
            "Landshut",
            "Passau",
            "Regen",
            "Rottal-Inn",
            "Straubing-Bogen",
            "Dingolfing-Landau",
            "Amberg",
            "Regensburg",
            "Weiden i.d. OPf.",
            "Amberg-Sulzbach",
            "Cham",
            "Neumarkt i.d. OPf.",
            "Neustadt a.d. Waldnaab",
            "Regensburg",
            "Schwandorf",
            "Tirschenreuth",
            "Bamberg",
            "Bayreuth",
            "Coburg",
            "Hof",
            "Bamberg",
            "Bayreuth",
            "Coburg",
            "Forchheim",
            "Hof",
            "Kronach",
            "Kulmbach",
            "Lichtenfels",
            "Wunsiedel i. Fichtelgebirge",
            "Ansbach",
            "Erlangen",
            "Fürth",
            "Nürnberg",
            "Schwabach",
            "Ansbach",
            "Erlangen-Höchstadt",
            "Fürth",
            "Nürnberger Land",
            "Neustadt a.d. Aisch-Bad Windsheim",
            "Roth",
            "Weißenburg-Gunzenhausen",
            "Aschaffenburg",
            "Schweinfurt",
            "Würzburg",
            "Aschaffenburg",
            "Bad Kissingen",
            "Rhön-Grabfeld",
            "Haßberge",
            "Kitzingen",
            "Miltenberg",
            "Main-Spessart",
            "Schweinfurt",
            "Würzburg",
            "Augsburg",
            "Kaufbeuren",
            "Kempten (Allgäu)",
            "Memmingen",
            "Aichach-Friedberg",
            "Augsburg",
            "Dillingen a.d. Donau",
            "Günzburg",
            "Neu-Ulm",
            "Lindau (Bodensee)",
            "Ostallgäu",
            "Unterallgäu",
            "Donau-Ries",
            "Oberallgäu",
            "Regionalverband Saarbrücken",
            "Merzig-Wadern",
            "Neunkirchen",
            "Saarlouis",
            "Saarpfalz-Kreis",
            "St. Wendel",
            "Brandenburg an der Havel",
            "Cottbus",
            "Frankfurt (Oder)",
            "Potsdam",
            "Barnim",
            "Dahme-Spreewald",
            "Elbe-Elster",
            "Havelland",
            "Märkisch-Oderland",
            "Oberhavel",
            "Oberspreewald-Lausitz",
            "Oder-Spree",
            "Ostprignitz-Ruppin",
            "Potsdam-Mittelmark",
            "Prignitz",
            "Spree-Neiße",
            "Teltow-Fläming",
            "Uckermark",
            "Rostock",
            "Schwerin",
            "Mecklenburgische Seenplatte",
            "Rostock",
            "Vorpommern-Rügen",
            "Nordwestmecklenburg",
            "Vorpommern-Greifswald",
            "Ludwigslust-Parchim",
            "Chemnitz",
            "Erzgebirgskreis",
            "Mittelsachsen",
            "Vogtlandkreis",
            "Zwickau",
            "Dresden",
            "Bautzen",
            "Görlitz",
            "Meißen",
            "Sächsische Schweiz-Osterzgebirge",
            "Leipzig",
            "Leipzig",
            "Nordsachsen",
            "Dessau-Roßlau",
            "Halle (Saale)",
            "Magdeburg",
            "Altmarkkreis Salzwedel",
            "Anhalt-Bitterfeld",
            "Börde",
            "Burgenlandkreis",
            "Harz",
            "Jerichower Land",
            "Mansfeld-Südharz",
            "Saalekreis",
            "Salzlandkreis",
            "Stendal",
            "Wittenberg",
            "Erfurt",
            "Gera",
            "Jena",
            "Suhl",
            "Weimar",
            "Eisenach",
            "Eichsfeld",
            "Nordhausen",
            "Wartburgkreis",
            "Unstrut-Hainich-Kreis",
            "Kyffhäuserkreis",
            "Schmalkalden-Meiningen",
            "Gotha",
            "Sömmerda",
            "Hildburghausen",
            "Ilm-Kreis",
            "Weimarer Land",
            "Sonneberg",
            "Saalfeld-Rudolstadt",
            "Saale-Holzland-Kreis",
            "Saale-Orla-Kreis",
            "Greiz",
            "Altenburger Land",
            "Berlin Reinickendorf",
            "Berlin Charlottenburg-Wilmersdorf",
            "Berlin Treptow-Köpenick",
            "Berlin Pankow",
            "Berlin Neukölln",
            "Berlin Lichtenberg",
            "Berlin Marzahn-Hellersdorf",
            "Berlin Spandau",
            "Berlin Steglitz-Zehlendorf",
            "Berlin Mitte",
            "Berlin Friedrichshain-Kreuzberg",
            "Berlin Tempelhof-Schöneberg"
        )
    }
}