package cz.muni.stanse.gui;

final class MakefileSourceEnumerator extends
                                           ReferencedSourceCodeFilesEnumerator {
    // package-private section

    MakefileSourceEnumerator(final String makeFile) {
        super(makeFile);
    }

    @Override
    java.util.List<String> getSourceCodeFiles() throws Exception {
        java.util.List<String> result;
        synchronized(getClass()) {
            final String batchFile = createBatchFile(getReferenceFile());
            result = new BatchFileEnumerator(batchFile).getSourceCodeFiles();
            new java.io.File(batchFile).delete();
        }
        return result;
    }

    // private section

    private static String createBatchFile(final String makeFile)
                                                              throws Exception {
        final String batchFile = "/tmp/tmp_stanse_batch_file_for_makefile.txt";
        final ProcessBuilder builder = new ProcessBuilder("make","CC=stcc");
        final java.util.Map<String,String> environment = builder.environment();
        environment.put("JOB_FILE",batchFile);
        environment.put("PATH", environment.get("PATH") + ":" +
                                new java.io.File(".").getAbsolutePath());
        builder.directory(new java.io.File(makeFile).getParentFile())
               .start()
               .waitFor();
        return batchFile;
    }
}
