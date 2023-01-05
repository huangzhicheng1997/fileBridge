package com.github.fileBridge.devtools;

import com.github.fileBridge.common.se.LuaValueSerializer;
import com.github.fileBridge.common.se.ScriptsAccessor;
import com.github.fileBridge.common.utils.FileUtil;
import com.github.fileBridge.common.utils.StringUtils;
import org.apache.commons.cli.*;
import org.luaj.vm2.LuaValue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Scanner;

/**
 * @author ZhiCheng
 * @date 2022/12/13 18:02
 */
public class Main {
    private static final CommandLineParser commandLineParser = new DefaultParser();
    private static final HelpFormatter helpFormatter = new HelpFormatter();


    public static void main(String[] args) throws IOException {
        Options options = new Options();
        Option luaOption = Option.builder().option("t").longOpt("test")
                .argName("test")
                .hasArg()
                .required()
                .desc("test tool lua or pattern").build();
        options.addOption(luaOption);
        try {
            CommandLine cmd = commandLineParser.parse(options, args);
            if (cmd.hasOption("t")) {
                String toolName = cmd.getOptionValue("t");
                switch (toolName) {
                    case "lua" -> accessLuaTool();
                    case "pattern" -> accessPatternTool();
                }
            }
            throw new ParseException("illegal cmd");
        } catch (ParseException parseException) {
            helpFormatter.printHelp(" ", options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void accessPatternTool() {
        System.out.println("\nuse pattern test tool.");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nset pattern:");
            String pattern = nextLine(scanner);
            System.out.println("\nset target:");
            String target = nextLine(scanner);
            boolean matches = target.matches(pattern);
            System.out.println("\nresult:" + matches);
        }

    }

    private static void accessLuaTool() throws IOException {
        System.out.println("\nuse lua test tool.");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nset luaDir:");
            String luaDir = nextLine(scanner);
            System.out.println("\nset target lua scriptName:");
            String scriptName = nextLine(scanner);

            File script = FileUtil.newFile(luaDir, scriptName);
            if (!script.exists() || StringUtils.isBlank(luaDir) || StringUtils.isBlank(scriptName)) {
                System.out.println("\nscript " + scriptName + " not found.");
                continue;
            }

            System.setProperty("log.agent.lua.path", luaDir);
            ScriptsAccessor scriptsAccessor = ScriptsAccessor.newScriptLoader();

            while (true) {
                System.out.println("\nlog:");
                String log = nextLine(scanner);
                ScriptsAccessor.LuaFunctionVisitor mappings = scriptsAccessor.newLuaFunctionVisitor(scriptName, "mappings");
                Map<String, String> map = LuaValueSerializer.toStringMap(mappings.invokeOneResultFunc(new LuaValue[]{LuaValue.valueOf(log)}));
                System.out.println(map);
            }
        }
    }


    private static String nextLine(Scanner scanner) {
        if (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            if ("exit".equals(cmd)) {
                System.exit(0);
            }
            return cmd;
        }
        return "";
    }
}
