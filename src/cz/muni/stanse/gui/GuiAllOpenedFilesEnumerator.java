package cz.muni.stanse.gui;

import cz.muni.stanse.SourceCodeFilesEnumerator;

public final class GuiAllOpenedFilesEnumerator extends SourceCodeFilesEnumerator {
    @Override
    public java.util.List<String> getSourceCodeFiles() throws Exception {
        return GuiMainWindow.getInstance().getOpenedSourceFilesManager().
                                                                 getAllFiles();
    }
}
