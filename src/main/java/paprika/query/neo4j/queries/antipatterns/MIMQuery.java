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

package paprika.query.neo4j.queries.antipatterns;

import paprika.query.neo4j.queries.PaprikaQuery;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class MIMQuery extends PaprikaQuery {

    public static final String KEY = "MIM";

    public MIMQuery() {
        super(KEY);
    }

    @Override
    public String getQuery(boolean details) {
        String query = " MATCH (m1:Method)\n" +
                "WHERE m1.number_of_callers > 0 m1.is_static = false\n" +
                "   AND m1.is_override = false\n" +
                "   AND NOT (m1)-[:USES]->(:Variable)\n" +
                "   AND NOT (m1)-[:CALLS]->(:ExternalMethod)\n" +
                "   AND NOT (m1)-[:CALLS]->(:Method)\n" +
                "   AND m1.is_init = false\n" +
                "RETURN m1.app_key as app_key, m1.full_name as full_name, labels(m1)[0] as `LABEL[0]`";
        return query;
    }

}
