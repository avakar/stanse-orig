package cz.muni.stanse.automatonchecker;

import cz.muni.stanse.utils.Pair;
import cz.muni.stanse.utils.Trinity;
import cz.muni.stanse.utils.XMLAlgo;

import java.util.HashMap;
import java.util.Vector;

final class XMLTransitionRule {

    // package-private section

    XMLTransitionRule(final org.dom4j.Element XMLtransitionElement,
                      final HashMap<String,Integer> statesSymbolTable)
                                                              throws Exception {
        final Trinity<String,Vector<String>,Character> fromSymbol =
            XMLRuleStringParser.parseOneSymbolRuleString(
                XMLAlgo.readValueOfAttribute("from",XMLtransitionElement));
        checkMode(fromSymbol.getThird());
        checkVars(1,-1,fromSymbol.getSecond().size());
        inSymbolID = statesSymbolTable.get(fromSymbol.getFirst());

        final String toString =
            XMLAlgo.readValueOfAttribute("to",XMLtransitionElement);
        final Trinity<String,Vector<String>,Character> toSymbol =
            (toString.isEmpty()) ?
                new Trinity<String,Vector<String>,Character>
                                            ("",new Vector<String>(),'+') :
                XMLRuleStringParser.parseOneSymbolRuleString(toString);
        checkMode(toSymbol.getThird());
        if (!toString.isEmpty())
            checkVars(1,-1,toSymbol.getSecond().size());
        final Integer outSymbol = statesSymbolTable.get(toSymbol.getFirst());
        outSymbolID = (outSymbol == null) ? -1 : (int)outSymbol;

        final Trinity<String,Vector<String>,Character> bySymbol =
            XMLRuleStringParser.parseOneSymbolRuleString(
                XMLAlgo.readValueOfAttribute("by",XMLtransitionElement));
        checkMode(bySymbol.getThird());
        checkVars(1,1,bySymbol.getSecond().size());
        matchFlags = buildMatchFlags(fromSymbol.getSecond(),
                                     bySymbol.getSecond().firstElement());
        matchOutIndices = buildMatchOutIndices(fromSymbol.getSecond(),
                                           toSymbol.getSecond(),
                                           bySymbol.getSecond().firstElement());
        patternName = bySymbol.getFirst();
    }

    String getPatternName() {
        return patternName;
    }

    Pair<Boolean,AutomatonState>
    transformAutomatonState(final AutomatonState state, final int automatonID) {
        if (state.getSymbolID() == getInSymbolID() &&
                matchIDsMatchesLocationID(state.getAutomatonIDs(),automatonID))
            return new Pair<Boolean,AutomatonState>(
                         true,buildResultState(state.getCFG(),
                                           state.getAutomatonIDs(),automatonID));
        return new Pair<Boolean,AutomatonState>(false,null);
    }

    // private section

    private boolean matchIDsMatchesLocationID(
                    final Vector<Integer> stateMatchIDs, final int locationID) {
        if (stateMatchIDs.size() != getMatchFlags().size())
            return false;
        for (int i = 0; i < stateMatchIDs.size(); ++i)
            if (( getMatchFlags().get(i) && stateMatchIDs.get(i) != locationID) ||
                (!getMatchFlags().get(i) && stateMatchIDs.get(i) == locationID) )
                return false;
        return true;
    }

    private AutomatonState buildResultState(
                        final cz.muni.stanse.xml2cfg.ControlFlowGraph CFG,
                        final Vector<Integer> matchIDs, final int locationID) {
        if (getOutSymbolID() == -1)
            return null;
        final Vector<Integer> outMatchIDs =
            new Vector<Integer>(getMatchOutIndices().size());
        for (int i = 0; i < getMatchOutIndices().size(); ++i) {
            assert(getMatchOutIndices().get(i) < matchIDs.size());
            outMatchIDs.add((getMatchOutIndices().get(i) == -1) ? locationID :
                                    matchIDs.get(getMatchOutIndices().get(i)));
        }
        return new AutomatonState(CFG,getOutSymbolID(),outMatchIDs);
    }

    private static Vector<Boolean> buildMatchFlags(final Vector<String> matchIDs,
                                                 final String locationVarName) {
        final Vector<Boolean> matchFlags = new Vector<Boolean>(matchIDs.size());
        for (int i = 0; i < matchIDs.size(); ++i)
            matchFlags.add(matchIDs.get(i).equals(locationVarName));
        return matchFlags;
    }

    private static Vector<Integer> buildMatchOutIndices(
                                final Vector<String> fromVarNames,
                                final Vector<String> toVarNames,
                                final String locationVarName) throws Exception {
        final Vector<Integer> matchIndices =
        new Vector<Integer>(toVarNames.size()); 
        for (int i = 0; i < toVarNames.size(); ++i)
            matchIndices.add((toVarNames.get(i).equals(locationVarName)) ?
                              -1 : findVarName(fromVarNames,toVarNames.get(i)));

        return matchIndices;
    }

    private static int findVarName(final Vector<String> varNames,
            final String searchedName) throws Exception {
        final int index = varNames.indexOf(searchedName);
        if (index != -1)
           return index;
        throw new Exception("[stanse/AutomatonChecker] - " +
                "XMLTransitionRule.findVarName() :: " +
                "error: XML transition syntax error -> variable '" +
                searchedName + "' in attribute 'to' cannot be found in 'from'" +
                " attribute of the transition [possibilities are: " + varNames);
    }

    private static void checkMode(final char mode) throws Exception {
        if (mode != '+')
            throw new Exception("[stanse/AutomatonChecker] - " +
                    "XMLTransitionRule.checkMode() :: " +
                    "error: XML rule syntax error -> Invalid mode of symbol. " +
                    "Mode can only be nothing or symbol '+'.");
    }

    private static void checkVars(final int minVars, final int maxVars,
                                  final int numVars) throws Exception {
        if ((minVars >= 0 && numVars < minVars) ||
            (maxVars >= 0 && numVars > maxVars) )
            throw new Exception("[stanse/AutomatonChecker] - " +
                "XMLTransitionRule.checkVars() :: " +
                "error: XML rule syntax error -> Invalid number of variables.");
    }

    private int getInSymbolID() {
        return inSymbolID;
    }

    private int getOutSymbolID() {
        return outSymbolID;
    }

    private Vector<Boolean> getMatchFlags() {
        return matchFlags;
    }

    private Vector<Integer> getMatchOutIndices() {
        return matchOutIndices;
    }

    private final String patternName;
    private final int inSymbolID;
    private final int outSymbolID;
    private final Vector<Boolean> matchFlags;
    private final Vector<Integer> matchOutIndices;
}