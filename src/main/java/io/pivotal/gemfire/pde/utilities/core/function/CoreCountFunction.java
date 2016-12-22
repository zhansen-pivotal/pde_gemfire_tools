package io.pivotal.gemfire.pde.utilities.core.function;

/**
 * Created by zhansen on 12/22/16.
 */


import io.pivotal.gemfire.pde.utilities.core.util.OSValidator;
import org.slf4j.Logger;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class CoreCountFunction implements Function, Declarable {

    private static final String ID = "CoreCountFunction";
    private static final Logger LOG = LoggerFactory.getLogger(CoreCountFunction.class);

    @Override
    public void init(Properties properties) {

    }


    @Override
    public void execute(FunctionContext functionContext) {
        LOG.info("Retrieving count on cluster");
        OSValidator osValidator = new OSValidator();
        String command = "";
        if (osValidator.isMac()) {
            LOG.info("OS Verified as [ Mac] ");
            command = "sysctl -n machdep.cpu.core_count";
        } else if (osValidator.isUnix()) {
            LOG.info("OS Verified as [ UNIX ] ");
            command = "lscpu";
        } else if (osValidator.isWindows()) {
            LOG.info("OS Verified as [ WINDOWS] ");
            command = "cmd /C WMIC CPU Get /Format:List";
        }
        Process process = null;
        int numberOfCores = 0;
        int sockets = 0;
        try {
            if (osValidator.isMac()) {
                String[] cmd = {"/bin/sh", "-c", command};
                process = Runtime.getRuntime().exec(cmd);
            } else {
                process = Runtime.getRuntime().exec(command);
            }
        } catch (Exception e) {
            LOG.error("Exception Raised in {}, Exception=[{}]", CoreCountFunction.ID, e.getMessage());
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (osValidator.isMac()) {
                    numberOfCores = line.length() > 0 ? Integer.parseInt(line) : 0;
                } else if (osValidator.isUnix()) {
                    if (line.contains("Core(s) per socket:")) {
                        numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                    }
                    if (line.contains("Socket(s):")) {
                        sockets = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                    }
                } else if (osValidator.isWindows()) {
                    if (line.contains("NumberOfCores")) {
                        numberOfCores = Integer.parseInt(line.split("=")[1]);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception Raised in {}, Exception=[{}]", CoreCountFunction.ID, e.getMessage());
        }
        if (osValidator.isUnix()) {
            LOG.info("Function Return: CoreCount={}", numberOfCores * sockets);
            functionContext.getResultSender().lastResult(numberOfCores * sockets);
        }
        LOG.info("Function Return: CoreCount={}", numberOfCores);
        functionContext.getResultSender().lastResult(numberOfCores);
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public String getId() {
        return CoreCountFunction.ID;
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return true;
    }
}