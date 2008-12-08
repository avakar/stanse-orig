package cz.muni.stanse.automatonchecker;

import cz.muni.stanse.parser.CFGEdge;

import cz.muni.stanse.utils.Pair;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Vector;

final class PatternLocationCreator extends cz.muni.stanse.utils.CFGvisitor {

    // public section

    @Override
    public boolean visit(final CFGEdge edge, final org.dom4j.Element element)
                                                              throws Exception {
        final Vector<XMLPattern> XMLpatterns =
            getXMLAutomatonDefinition().getXMLpatterns();
        for (int i = 0; i < XMLpatterns.size(); ++i) {
            final Pair<Boolean,PatternVariablesAssignment>
                matchResult = XMLpatterns.get(i).matchesXMLElement(element);
            if (matchResult.getFirst()) {
                Integer automatonID = getAutomataIDs().get(
                                                matchResult.getSecond());
                if (automatonID == null)
                    getAutomataIDs().put(matchResult.getSecond(),
                                         automatonID = getUniqueAutomatonID());
                getCreatedPatternLocations().put(edge,
                        createPatternLocation(i,edge,automatonID) );
                break;
            }
        }
        return true;
    }

    // package-private section

    PatternLocationCreator(
            final cz.muni.stanse.parser.ControlFlowGraph cfg,
            final XMLAutomatonDefinition XMLdefinition) {
        super();
        CFG = cfg;
        automatonDefinition = XMLdefinition;
        createdPatternLocations = new HashMap<CFGEdge,PatternLocation>();

        automataIDs = new HashMap<PatternVariablesAssignment,Integer>();

        patternAutomataCounter = 0;
        
        createEntryLocation();
        createExitLocation();
    }

    HashMap<CFGEdge,PatternLocation> getCreatedPatternLocations() {
        return createdPatternLocations; 
    }

    int getNumDistinctLocations() {
        return getAutomataIDs().size();
    }

    // private section

    private void createEntryLocation() {
        final CFGEdge edge = getCFG().getEntryEdge();
        getCreatedPatternLocations().put(edge,
            new PatternLocation(getCFG(),edge,new LinkedList<TransitionRule>(),
                                new LinkedList<ErrorRule>()));
    }

    private void createExitLocation() {
        final CFGEdge edge = getCFG().getExitEdge();
        getCreatedPatternLocations().put(edge,
            new PatternLocation(getCFG(),edge,new LinkedList<TransitionRule>(),
                                new LinkedList<ErrorRule>()));
    }

    private PatternLocation createPatternLocation(final int patternIndex,
                                    final CFGEdge edge, final int automatonID) {
        final LinkedList<TransitionRule> transitionRules =
                new LinkedList<TransitionRule>();
        for (final XMLTransitionRule XMLtransitionRule :
             getXMLAutomatonDefinition().
                                  getXMLtransitionRulesForPattern(patternIndex))
            transitionRules.add(new TransitionRule(XMLtransitionRule,
                                                   automatonID));

        final LinkedList<ErrorRule> errorRules = new LinkedList<ErrorRule>();
        for (final XMLErrorRule XMLerrorRule : getXMLAutomatonDefinition().
                                       getXMLerrorRulesForPattern(patternIndex))
            errorRules.add(new ErrorRule(XMLerrorRule,automatonID));

        return new PatternLocation(getCFG(),edge,transitionRules,errorRules);
    }

    private cz.muni.stanse.parser.ControlFlowGraph getCFG() {
        return CFG;
    }

    private XMLAutomatonDefinition getXMLAutomatonDefinition() {
        return automatonDefinition;
    }

    private HashMap<PatternVariablesAssignment,Integer>
    getAutomataIDs() {
        return automataIDs;
    }

    private int getUniqueAutomatonID() {
        return patternAutomataCounter++;
    }

    private final cz.muni.stanse.parser.ControlFlowGraph CFG;
    private final XMLAutomatonDefinition automatonDefinition;
    private final HashMap<CFGEdge,PatternLocation> createdPatternLocations;
    private final HashMap<PatternVariablesAssignment,Integer> automataIDs;
    private int patternAutomataCounter; 
}
