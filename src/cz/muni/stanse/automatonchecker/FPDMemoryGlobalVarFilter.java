package cz.muni.stanse.automatonchecker;

import cz.muni.stanse.codestructures.CFGHandle;
import cz.muni.stanse.codestructures.CFGNode;
import cz.muni.stanse.codestructures.LazyInternalStructures;
import cz.muni.stanse.utils.Pair;

import java.util.Map;

import org.dom4j.Element;

/**
 * Lowers importance when a variable with allocated memory is global and
 * memory leak is reported.
 * @author xslaby
 */
final class FPDMemoryGlobalVarFilter extends FalsePositivesDetector {
    private LazyInternalStructures lis;

    public FPDMemoryGlobalVarFilter(LazyInternalStructures internals) {
	lis = internals;
    }

    @Override
    int getTraceImportance(final java.util.List<CFGNode> path,
                            final java.util.Stack<CFGNode> cfgContext,
                            final ErrorRule rule) {
        if (!rule.getErrorDescription().equals(
		"memory leak - leaving function without releasing memory"))
            return getBugImportance(0);
	CFGNode start = path.get(0);
        Element ass = start.getElement();
        if (!ass.getName().equals("assignExpression"))
            return getBugImportance(0);
        Element left = (Element)ass.elements().get(0); // leftside
	if (!left.getName().equals("id"))
	    return getBugImportance(0);
	CFGHandle cfg = lis.getNodeToCFGdictionary().get(start);
	if (!cfg.isSymbolLocal(left.getText()))
	    return getFalsePositiveImportance();
        return getBugImportance(0);
    }
}
final class FPDMemoryGlobalVarFilterCreator
        extends FalsePositivesDetectorCreator {

    @Override
    boolean isApplicable(final XMLAutomatonDefinition definition,
                         boolean isInterprocediral) {
        return definition.getAutomatonName().equals(
                "Linux kernel pointer analysis automaton checker");
    }

    @Override
    FalsePositivesDetector create(XMLAutomatonDefinition definition,
        LazyInternalStructures internals,boolean isInterprocediral,
        final Map<CFGNode,Pair<PatternLocation,PatternLocation>>
                                                       nodeLocationDictionary) {
        return new FPDMemoryGlobalVarFilter(internals);
    }
}
