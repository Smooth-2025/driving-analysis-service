// reports/accident_reaction/snapshot/AccidentReactionSnapshotWriter.java
package com.smooth.driving_analysis_service.reports.accident_reaction.snapshot;
public interface AccidentReactionSnapshotWriter {
    void upsertV2xResponse(Long reportId, String snapshotType); // INTERIM/FINAL 공통
}
