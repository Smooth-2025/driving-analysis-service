// reports/accident_reaction/resolver/DrivingResolver.java
package com.smooth.driving_analysis_service.reports.accident_reaction.resolver;
public interface DrivingResolver {
    String resolveDrivingId(Long userId, long renderedAtMs, int bufferSec);
}
