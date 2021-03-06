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
            "L??beck",
            "Neum??nster",
            "Dithmarschen",
            "Herzogtum Lauenburg",
            "Nordfriesland",
            "Ostholstein",
            "Pinneberg",
            "Pl??n",
            "Rendsburg-Eckernf??rde",
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
            "Wolfenb??ttel",
            "G??ttingen",
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
            "L??chow-Dannenberg",
            "L??neburg",
            "Osterholz",
            "Rotenburg (W??mme)",
            "Heidekreis",
            "Stade",
            "Uelzen",
            "Verden",
            "Delmenhorst",
            "Emden",
            "Oldenburg (Oldb)",
            "Osnabr??ck",
            "Wilhelmshaven",
            "Ammerland",
            "Aurich",
            "Cloppenburg",
            "Emsland",
            "Friesland",
            "Grafschaft Bentheim",
            "Leer",
            "Oldenburg",
            "Osnabr??ck",
            "Vechta",
            "Wesermarsch",
            "Wittmund",
            "Bremen",
            "Bremerhaven",
            "D??sseldorf",
            "Duisburg",
            "Essen",
            "Krefeld",
            "M??nchengladbach",
            "M??lheim an der Ruhr",
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
            "K??ln",
            "Leverkusen",
            "St??dteregion Aachen",
            "D??ren",
            "Rhein-Erft-Kreis",
            "Euskirchen",
            "Heinsberg",
            "Oberbergischer Kreis",
            "Rheinisch-Bergischer Kreis",
            "Rhein-Sieg-Kreis",
            "Bottrop",
            "Gelsenkirchen",
            "M??nster",
            "Borken",
            "Coesfeld",
            "Recklinghausen",
            "Steinfurt",
            "Warendorf",
            "Bielefeld",
            "G??tersloh",
            "Herford",
            "H??xter",
            "Lippe",
            "Minden-L??bbecke",
            "Paderborn",
            "Bochum",
            "Dortmund",
            "Hagen",
            "Hamm",
            "Herne",
            "Ennepe-Ruhr-Kreis",
            "Hochsauerlandkreis",
            "M??rkischer Kreis",
            "Olpe",
            "Siegen-Wittgenstein",
            "Soest",
            "Unna",
            "Darmstadt",
            "Frankfurt am Main",
            "Offenbach am Main",
            "Wiesbaden",
            "Bergstra??e",
            "Darmstadt-Dieburg",
            "Gro??-Gerau",
            "Hochtaunuskreis",
            "Main-Kinzig-Kreis",
            "Main-Taunus-Kreis",
            "Odenwaldkreis",
            "Offenbach",
            "Rheingau-Taunus-Kreis",
            "Wetteraukreis",
            "Gie??en",
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
            "Werra-Mei??ner-Kreis",
            "Koblenz",
            "Ahrweiler",
            "Altenkirchen (Westerwald)",
            "Bad Kreuznach",
            "Birkenfeld",
            "Cochem-Zell",
            "Mayen-Koblenz",
            "Neuwied",
            "Rhein-Hunsr??ck-Kreis",
            "Rhein-Lahn-Kreis",
            "Westerwaldkreis",
            "Trier",
            "Bernkastel-Wittlich",
            "Eifelkreis Bitburg-Pr??m",
            "Vulkaneifel",
            "Trier-Saarburg",
            "Frankenthal (Pfalz)",
            "Kaiserslautern",
            "Landau in der Pfalz",
            "Ludwigshafen am Rhein",
            "Mainz",
            "Neustadt an der Weinstra??e",
            "Pirmasens",
            "Speyer",
            "Worms",
            "Zweibr??cken",
            "Alzey-Worms",
            "Bad D??rkheim",
            "Donnersbergkreis",
            "Germersheim",
            "Kaiserslautern",
            "Kusel",
            "S??dliche Weinstra??e",
            "Rhein-Pfalz-Kreis",
            "Mainz-Bingen",
            "S??dwestpfalz",
            "Stuttgart",
            "B??blingen",
            "Esslingen",
            "G??ppingen",
            "Ludwigsburg",
            "Rems-Murr-Kreis",
            "Heilbronn",
            "Heilbronn",
            "Hohenlohekreis",
            "Schw??bisch Hall",
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
            "L??rrach",
            "Waldshut",
            "Reutlingen",
            "T??bingen",
            "Zollernalbkreis",
            "Ulm",
            "Alb-Donau-Kreis",
            "Biberach",
            "Bodenseekreis",
            "Ravensburg",
            "Sigmaringen",
            "Ingolstadt",
            "M??nchen",
            "Rosenheim",
            "Alt??tting",
            "Berchtesgadener Land",
            "Bad T??lz-Wolfratshausen",
            "Dachau",
            "Ebersberg",
            "Eichst??tt",
            "Erding",
            "Freising",
            "F??rstenfeldbruck",
            "Garmisch-Partenkirchen",
            "Landsberg am Lech",
            "Miesbach",
            "M??hldorf a. Inn",
            "M??nchen",
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
            "F??rth",
            "N??rnberg",
            "Schwabach",
            "Ansbach",
            "Erlangen-H??chstadt",
            "F??rth",
            "N??rnberger Land",
            "Neustadt a.d. Aisch-Bad Windsheim",
            "Roth",
            "Wei??enburg-Gunzenhausen",
            "Aschaffenburg",
            "Schweinfurt",
            "W??rzburg",
            "Aschaffenburg",
            "Bad Kissingen",
            "Rh??n-Grabfeld",
            "Ha??berge",
            "Kitzingen",
            "Miltenberg",
            "Main-Spessart",
            "Schweinfurt",
            "W??rzburg",
            "Augsburg",
            "Kaufbeuren",
            "Kempten (Allg??u)",
            "Memmingen",
            "Aichach-Friedberg",
            "Augsburg",
            "Dillingen a.d. Donau",
            "G??nzburg",
            "Neu-Ulm",
            "Lindau (Bodensee)",
            "Ostallg??u",
            "Unterallg??u",
            "Donau-Ries",
            "Oberallg??u",
            "Regionalverband Saarbr??cken",
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
            "M??rkisch-Oderland",
            "Oberhavel",
            "Oberspreewald-Lausitz",
            "Oder-Spree",
            "Ostprignitz-Ruppin",
            "Potsdam-Mittelmark",
            "Prignitz",
            "Spree-Nei??e",
            "Teltow-Fl??ming",
            "Uckermark",
            "Rostock",
            "Schwerin",
            "Mecklenburgische Seenplatte",
            "Rostock",
            "Vorpommern-R??gen",
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
            "G??rlitz",
            "Mei??en",
            "S??chsische Schweiz-Osterzgebirge",
            "Leipzig",
            "Leipzig",
            "Nordsachsen",
            "Dessau-Ro??lau",
            "Halle (Saale)",
            "Magdeburg",
            "Altmarkkreis Salzwedel",
            "Anhalt-Bitterfeld",
            "B??rde",
            "Burgenlandkreis",
            "Harz",
            "Jerichower Land",
            "Mansfeld-S??dharz",
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
            "Kyffh??userkreis",
            "Schmalkalden-Meiningen",
            "Gotha",
            "S??mmerda",
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
            "Berlin Treptow-K??penick",
            "Berlin Pankow",
            "Berlin Neuk??lln",
            "Berlin Lichtenberg",
            "Berlin Marzahn-Hellersdorf",
            "Berlin Spandau",
            "Berlin Steglitz-Zehlendorf",
            "Berlin Mitte",
            "Berlin Friedrichshain-Kreuzberg",
            "Berlin Tempelhof-Sch??neberg"
        )
    }
}