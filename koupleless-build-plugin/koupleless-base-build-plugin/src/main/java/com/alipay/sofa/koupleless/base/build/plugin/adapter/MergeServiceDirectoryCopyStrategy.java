package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class MergeServiceDirectoryCopyStrategy implements CopyAdapterStrategy {

    public void mergeFile(File inputServiceFile, File outputServiceFile) throws Throwable {
        List<String> mergedLines = null;
        List<String> adapterLines = Files.readAllLines(inputServiceFile.toPath());
        List<String> originalLines = Files.readAllLines(outputServiceFile.toPath());
        mergedLines = new ArrayList<>(originalLines);
        Collection<String> deltaLines = CollectionUtils.subtract(adapterLines, originalLines);
        mergedLines.addAll(deltaLines);
        Files.write(outputServiceFile.toPath(), mergedLines, TRUNCATE_EXISTING);
    }

    @Override
    public void copy(File adapter, File build) throws Throwable {
        Preconditions.checkState(adapter.isDirectory(), "in must be a directory");
        Preconditions.checkState(build.isDirectory(), "out must be a directory");
        Collection<File> adapterFiles = FileUtils.listFiles(adapter, null, false);
        Collection<File> buildFiles = FileUtils.listFiles(build, null, false);

        // Extract filenames for comparison
        Collection<String> inFileNames = adapterFiles.stream()
                .map(File::getName)
                .collect(Collectors.toList());
        Collection<String> outFileNames = buildFiles.stream()
                .map(File::getName)
                .collect(Collectors.toList());

        // firstly, lets find those files existed in input but not in output
        Collection<String> filesToCreate = CollectionUtils.subtract(inFileNames, outFileNames);

        for (File file : adapterFiles.stream().filter(
                f -> filesToCreate.contains(f.getName())
        ).collect(Collectors.toList())) {
            // normally there should be no dir in the input directory
            if (!file.isDirectory()) {
                File outputFile = Paths.get(build.getPath(), file.getName()).toFile();
                IOUtils.copy(Files.newInputStream(file.toPath()), Files.newOutputStream(outputFile.toPath()));
            }
        }

        // then, lets find those files existed in both input and output
        Collection<String> filesToMerge = CollectionUtils.intersection(inFileNames, outFileNames);
        for (File file : adapterFiles.stream().filter(
                f -> filesToMerge.contains(f.getName())
        ).collect(Collectors.toList())) {
            if (!file.isDirectory()) {
                File outputFile = Paths.get(build.getPath(), file.getName()).toFile();
                mergeFile(file, outputFile);
            }
        }
    }
}
