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

import paprika.analyse.entities.PaprikaClass;
import paprika.analyse.entities.PaprikaVariable;
import paprika.analyse.metrics.app.NumberOfClasses;
import paprika.analyse.metrics.app.NumberOfVariables;
import paprika.analyse.metrics.classes.condition.*;
import paprika.analyse.metrics.classes.condition.subclass.*;
import paprika.analyse.metrics.classes.stat.soot.DepthOfInheritance;
import paprika.analyse.metrics.classes.stat.soot.NumberOfAttributes;
import paprika.analyse.metrics.classes.stat.soot.NumberOfImplementedInterfaces;
import paprika.analyse.metrics.classes.stat.soot.SootClassStatistic;
import paprika.analyse.metrics.common.*;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.util.Chain;

import java.util.Map;

public class ClassProcessor {

    private CountedClassCondition[] classConditions = {
            new IsAbstractClass(),
            new IsInterface(),
            new IsInnerClassStatic()
    };

    private IsSubClass[] subClassConditions = {
            new IsActivity(),
            new IsApplication(),
            new IsAsyncTask(),
            new IsBroadcastReceiver(),
            new IsContentProvider(),
            new IsService(),
            new IsView()
    };

    private CommonCondition[] conditions = {
            new IsFinal(), // This must stay at index 0
            new IsStatic()
    };

    private SootClassStatistic[] statistics = {
            new DepthOfInheritance(),
            new NumberOfAttributes(),
            new NumberOfImplementedInterfaces(),
            new NumberOfMethods()
    };

    private PaprikaContainer container;
    private Map<SootClass, PaprikaClass> classMap;
    private boolean mainPackageOnly;
    private int varCount = 0;

    public ClassProcessor(PaprikaContainer container, boolean mainPackageOnly) {
        this.container = container;
        this.classMap = container.getClassMap();
        this.mainPackageOnly = mainPackageOnly;
    }

    public void processClasses() {
        Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
        String pack = container.getPaprikaApp().getPackage();
        String rsubClassStart = pack + ".R$";
        String packs = pack.concat(".");
        String buildConfigClass = pack.concat(".BuildConfig");
        for (SootClass sootClass : sootClasses) {
            String name = sootClass.getName();
            if (name.startsWith(rsubClassStart) || name.equals(buildConfigClass)) {
                continue;
            }
            if (!mainPackageOnly || name.startsWith(packs)) {
                collectClassMetrics(sootClass);
            }
        }
        // Now that all classes have been processed at least once (and the map filled) we can process NOC
        for (SootClass sootClass : sootClasses) {
            if (sootClass.hasSuperclass()) {
                SootClass superClass = sootClass.getSuperclass();
                PaprikaClass paprikaClass = classMap.get(superClass);
                if (paprikaClass != null) classMap.get(superClass).addChildren();
            }
        }
        collectAppMetrics();
    }

    private void collectClassMetrics(SootClass sootClass) {
        PaprikaClass paprikaClass = container.addClass(sootClass);
        // Checking if the class is final
        conditions[0].createIfMatching(sootClass, paprikaClass);
        // Checking if the class is a child of a relevant subclass
        for (IsSubClass subClass : subClassConditions) {
            if (subClass.createIfMatching(sootClass, paprikaClass)) {
                break;
            }
        }
        for (CountedClassCondition condition : classConditions) {
            condition.createIfMatching(sootClass, paprikaClass);
        }
        // Field analysis
        sootClass.getFields().forEach(field -> registerField(paprikaClass, field));
        // Numerical stats
        for (SootClassStatistic stat : statistics) {
            stat.collectMetric(sootClass, paprikaClass);
        }
    }

    private void registerField(PaprikaClass paprikaClass, SootField sootField) {
        PaprikaVariable paprikaVariable = container.addField(paprikaClass, sootField);
        varCount++;
        for (CommonCondition condition : conditions) {
            condition.createIfMatching(sootField, paprikaVariable);
        }
    }

    /**
     * Should be called after all classes have been processed once
     */
    private void collectAppMetrics() {
        NumberOfClasses.createNumberOfClasses(container.getPaprikaApp(), classMap.size());
        NumberOfVariables.createNumberOfVariables(container.getPaprikaApp(), varCount);
        for (CountedClassCondition condition : classConditions) {
            condition.createNumberMetric(container.getPaprikaApp());
        }
        for (IsSubClass condition : subClassConditions) {
            condition.createNumberMetric(container.getPaprikaApp());
        }
    }

}
