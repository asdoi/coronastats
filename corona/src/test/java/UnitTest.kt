import com.asdoi.corona.johnshopkins.JHUParser
import com.asdoi.corona.lgl.LGLParser
import com.asdoi.corona.rki.counties.RKICountiesParser
import com.asdoi.corona.rki.germany.RKIGermanyParser
import com.asdoi.corona.rki.states.RKIStatesParser
import com.asdoi.corona.unclassified.GovDEParser
import com.asdoi.corona.worldometers.continents.WmContinentsParser
import com.asdoi.corona.worldometers.countries.WmCountriesParser
import com.asdoi.corona.worldometers.states.WmStatesParser
import com.asdoi.corona.worldometers.world.WmWorldParser
import org.junit.Test

class UnitTest {

    @Test
    fun testRKI() {
        val card = JHUParser.parse(JHUParser.getSuggestions()[0])
        val card2 = LGLParser.parse(LGLParser.getSuggestions()[0])
        val card3 = RKICountiesParser.parse(RKICountiesParser.getSuggestions()[0]) //TODO
        val card4 = RKIGermanyParser.parse(RKIGermanyParser.getSuggestions()[0])
        val card5 = RKIStatesParser.parse(RKIStatesParser.getSuggestions()[0]) //TODO
        val card6 = GovDEParser.parse(GovDEParser.getSuggestions()[0])
        val card7 = WmContinentsParser.parse(WmContinentsParser.getSuggestions()[0]) //TODO
        val card8 = WmCountriesParser.parse(WmCountriesParser.getSuggestions()[0]) //TODO
        val card9 = WmStatesParser.parse(WmStatesParser.getSuggestions()[0]) //TODO
        val card10 = WmWorldParser.parse(WmWorldParser.getSuggestions()[0])
        println("")
    }

}