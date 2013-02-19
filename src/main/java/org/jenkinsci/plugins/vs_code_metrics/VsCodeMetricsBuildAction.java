package org.jenkinsci.plugins.vs_code_metrics;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.jenkinsci.plugins.vs_code_metrics.bean.*;
import org.jenkinsci.plugins.vs_code_metrics.util.CodeMetricsUtil;
import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;

/**
 * @author Yasuyuki Saito
 */
public class VsCodeMetricsBuildAction implements Action, StaplerProxy, HealthReportingAction {

    private final AbstractBuild<?,?> build;
    private transient WeakReference<CodeMetrics> resultRef = null;

    public VsCodeMetricsBuildAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public String getIconFileName() {
        return Constants.ACTION_ICON;
    }

    public String getDisplayName() {
        return Messages.VsCodeMetricsBuildAction_DisplayName();
    }

    public String getUrlName() {
        return Constants.ACTION_URL;
    }

    public Object getTarget() {
        return getReport();
    }

    public AbstractBuild<?,?> getBuild() {
        return build;
    }

    private CodeMetricsReport getReport() {
        CodeMetrics result = getCodeMetrics();
        return new CodeMetricsReport(build, result);
    }

    public void doGraph(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        CodeMetricsGraph graph = new CodeMetricsGraph(build, new String[0], build.getTimestamp(), Constants.GRAPH_WIDTH, Constants.GRAPH_HEIGHT);
        graph.doPng(req, rsp);
    }

    public HealthReport getBuildHealth() {
        CodeMetrics result = getCodeMetrics();
        if (result == null) return null;
        int maintainabilityIndex = Integer.valueOf(result.getMaintainabilityIndex());
        return new HealthReport(maintainabilityIndex, Messages._HealthReport_Description(maintainabilityIndex));
    }

    public synchronized CodeMetrics getCodeMetrics() {
        CodeMetrics result = null;
        if (resultRef != null) {
            result = resultRef.get();
            if (result != null) return result;
        }

        try {
            result = CodeMetricsUtil.getCodeMetrics(build);
            resultRef = new WeakReference<CodeMetrics>(result);
            return result;
        } catch (InterruptedException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
