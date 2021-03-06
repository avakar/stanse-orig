package cz.muni.stanse.configuration;

import cz.muni.stanse.Stanse;
import cz.muni.stanse.configuration.source_enumeration.SourceCodeFilesException;
import cz.muni.stanse.configuration.source_enumeration.SourceCodeFilesEnumerator;
import cz.muni.stanse.codestructures.Unit;
import cz.muni.stanse.codestructures.CFG;
import cz.muni.stanse.codestructures.CFGHandle;
import cz.muni.stanse.codestructures.LazyInternalStructures;
import cz.muni.stanse.codestructures.LazyInternalStructuresInter;
import cz.muni.stanse.codestructures.LazyInternalStructuresIntra;
import cz.muni.stanse.cparser.CUnit;
import cz.muni.stanse.utils.ClassLogger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

public final class SourceConfiguration {

    // public section

    public SourceConfiguration(final SourceCodeFilesEnumerator
                               sourceEnumerator) {
        this.sourceEnumerator = sourceEnumerator;
        units = null;
        cfgToUnitDictionary = null;
        internals = null;
	intraproceduralInternals = null;
    }

    public LazyInternalStructures getLazySourceInternals() {
        if (internals == null)
            setLazySourceInternals();
        return internals;
    }

    public LazyInternalStructures
		getLazySourceIntraproceduralInternals() {
	if (intraproceduralInternals == null)
            setLazySourceInternalsIntra();
	return intraproceduralInternals;
    }

    public SourceCodeFilesEnumerator getSourceEnumerator() {
        return sourceEnumerator;
    }

    // private section

    private List<Unit> getUnits() {
        if (units == null)
            setUnits();
        return units;
    }

    private List<CFGHandle> getCFGHandles() {
        List<CFGHandle> cfghs = new LinkedList<CFGHandle>();
        for (Unit unit: getUnits())
            cfghs.addAll(Stanse.getUnitManager().getCFGHandles(unit));
        return cfghs;
    }

    private synchronized void setLazySourceInternals() {
	if (internals != null)
	    return;

	internals = new LazyInternalStructuresInter(getUnits(), getCFGHandles());
    }

    private synchronized void setLazySourceInternalsIntra() {
	if (intraproceduralInternals != null)
	    return;

	intraproceduralInternals =
	    new LazyInternalStructuresIntra(getUnits(), getCFGHandles());
    }

    private synchronized void setUnits() {
        if (units == null)
            units = Collections.unmodifiableList(
                                             buildUnits(getSourceEnumerator()));
    }

    private static List<Unit>
    buildUnits(final SourceCodeFilesEnumerator sourceEnumerator) {
        final List<Unit> result = new LinkedList<Unit>();
        List <String> files;
        try {
            files = sourceEnumerator.getSourceCodeFiles();
        } catch (SourceCodeFilesException e) {
            ClassLogger.error(SourceConfiguration.class,
                              "Failed to get source files.", e);
            return Collections.unmodifiableList(result);
        }
        for (String pathName: files)
            result.add(new CUnit(pathName));
        return result;
    }

    private final SourceCodeFilesEnumerator sourceEnumerator;
    private List<Unit> units;
    private HashMap<CFG,Unit> cfgToUnitDictionary;
    private LazyInternalStructures internals;
    private LazyInternalStructures intraproceduralInternals;
}
