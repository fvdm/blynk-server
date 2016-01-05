package cc.blynk.server;

import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.dao.FileManager;
import cc.blynk.server.core.dao.ReportingDao;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.reporting.average.AverageAggregator;
import cc.blynk.server.core.stats.GlobalStats;
import cc.blynk.utils.Config;
import cc.blynk.utils.FileLoaderUtil;
import cc.blynk.utils.ServerProperties;

import static cc.blynk.utils.ReportingUtil.*;

/**
 * Just a holder for all necessary objects for server instance creation.
 *
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 28.09.15.
 */
public class Holder {

    public final TransportTypeHolder transportType;

    public final FileManager fileManager;

    public final SessionDao sessionDao;

    public final UserDao userDao;

    public final ReportingDao reportingDao;

    public final GlobalStats stats;

    public final ServerProperties props;
    public final AverageAggregator averageAggregator;
    public BlockingIOProcessor blockingIOProcessor;

    public Holder(ServerProperties serverProperties) {
        this.props = serverProperties;

        String dataFolder = serverProperties.getProperty("data.folder");

        this.transportType = new TransportTypeHolder(serverProperties);
        this.fileManager = new FileManager(dataFolder);
        this.sessionDao = new SessionDao();
        this.userDao = new UserDao(fileManager.deserialize());
        this.stats = new GlobalStats();
        final String reportingFolder = getReportingFolder(dataFolder);
        this.averageAggregator = new AverageAggregator(reportingFolder);
        this.reportingDao = new ReportingDao(reportingFolder, averageAggregator, serverProperties);

        this.blockingIOProcessor = new BlockingIOProcessor(
                serverProperties.getIntProperty("notifications.queue.limit", 10000),
                FileLoaderUtil.readFileAsString(Config.TOKEN_MAIL_BODY),
                reportingDao
        );
    }

    //for tests only
    public void setBlockingIOProcessor(BlockingIOProcessor blockingIOProcessor) {
        this.blockingIOProcessor = blockingIOProcessor;
    }
}
