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

package paprika.analyse.metrics.classes.stat.soot;

import paprika.analyse.entities.PaprikaClass;
import paprika.analyse.metrics.UnaryMetric;
import soot.SootClass;

/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class DepthOfInheritance implements SootClassStatistic {

    @Override
    public void collectMetric(SootClass sootClass, PaprikaClass paprikaClass) {
        UnaryMetric<Integer> metric = new UnaryMetric<>("depth_of_inheritance", paprikaClass,
                getDepthOfInheritance(sootClass));
        metric.updateEntity();
    }

    private int getDepthOfInheritance(SootClass sootClass) {
        int doi = 0;
        do {
            doi++;
            sootClass = sootClass.getSuperclass();
        } while (sootClass.hasSuperclass());
        return doi;
    }

}
