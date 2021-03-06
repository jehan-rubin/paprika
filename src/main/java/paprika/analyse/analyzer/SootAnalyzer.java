/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprika.analyse.analyzer;

import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.metrics.classes.stat.paprika.*;
import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.options.Options;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class SootAnalyzer extends Analyzer {

    private String androidJAR;
    private PaprikaContainer container;

    private List<PaprikaClassStatistic> finalMetrics = Arrays.asList(
            new ClassComplexity(),
            new CouplingBetweenObjects(),
            new LackOfCohesionInMethods(),
            new NPathComplexity(), // Must be done after class complexity has been processed
            new NumberOfChildren()
    );

    public SootAnalyzer(String apk, String androidJAR) {
        this.apk = apk;
        this.androidJAR = androidJAR;
    }

    @Override
    public void prepareSoot() {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_android_jars(androidJAR);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_output_format(Options.output_format_grimple);
        Options.v().set_output_dir(System.getProperty("user.home") + "/" + "/These/decompiler/out");
        Options.v().set_process_multiple_dex(true);
        Options.v().set_throw_analysis(Options.throw_analysis_dalvik);
        Options.v().set_no_bodies_for_excluded(true);
        PhaseOptions.v().setPhaseOption("cg", "all-reachable:true");
        PhaseOptions.v().setPhaseOption("gop", "enabled:true");
        List<String> excludeList = new LinkedList<>();
        excludeList.add("java.*");
        excludeList.add("sun.misc.*");
        excludeList.add("org.apache.*");
        excludeList.add("soot.*");
        excludeList.add("javax.servlet.*");
        Options.v().set_exclude(excludeList);
        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
        Scene.v().loadDynamicClasses();
        new CallGraphBuilder().build();
    }

    @Override
    public void runAnalysis(PaprikaApp app, boolean mainPackageOnly) {
        this.container = new PaprikaContainer(app);
        ClassProcessor classProcessor = new ClassProcessor(container, mainPackageOnly);
        MethodProcessor methodProcessor = new MethodProcessor(container);
        classProcessor.processClasses();
        methodProcessor.processMethods();
        computeFinalMetrics();
    }

    @Override
    public PaprikaApp getPaprikaApp() {
        return container.getPaprikaApp();
    }

    /**
     * Should be called last
     */
    public void computeFinalMetrics() {
        container.getPaprikaApp().getPaprikaClasses()
                .forEach(paprikaClass -> finalMetrics
                        .forEach(metric -> metric.collectMetric(paprikaClass)));
    }


}
