package jfutar.records.trip;

import jfutar.records.stop.Stop;
import jfutar.records.stop.StopTime;

import java.util.List;

/**
 * Ez a rekord tartalmazza egy konkrét utazás adatait.
 * A {@link jfutar.requests.GetTripDetails} API válasza alapján épül fel.
 *
 * @param stops a megállók listája időadatokkal
 * @param fromStop indulási megálló
 * @param toStop cél megálló
 * @param date az utazás dátuma
 */
public record Trip (
        List<StopTime> stops,
        Stop fromStop,
        Stop toStop,
        String date
) { }
