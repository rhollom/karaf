/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.shell.impl.console.parsing;

import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Parser;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.parsing.DefaultParser;
import org.apache.karaf.shell.support.parsing.GogoParser;

public class CommandLineParser {

    public static CommandLine buildCommandLine(Session session, String command, int cursor) {
        int pos = 0;
        while (true) {
            String rem = command.substring(pos);
            GogoParser cmdNameParser = new GogoParser(rem, rem.length());
            String name = cmdNameParser.value();
            name = session.resolveCommand(name);

            Parser cmdParser = null;
            for (Command cmd : session.getRegistry().getCommands()) {
                if (name.equals(cmd.getScope() + ":" + cmd.getName())) {
                    cmdParser = cmd.getParser();
                    break;
                }
            }
            if (cmdParser == null) {
                cmdParser = new DefaultParser();
            }

            CommandLine cmdLine = cmdParser.parse(session, rem, cursor - pos);
            int length = cmdLine.getBuffer().length();
            if (length < rem.length()) {
                char ch = rem.charAt(length);
                if (ch == ';' || ch == '|') {
                    length++;
                } else {
                    throw new IllegalArgumentException("Unrecognized character: '" + ch + "'");
                }
            }
            pos += length;
            if (cursor <= pos) {
                return cmdLine;
            }
        }
    }

    public static String parse(Session session, String command) {
        StringBuilder parsed = new StringBuilder();
        int pos = 0;
        while (pos < command.length()) {
            String rem = command.substring(pos);
            GogoParser cmdNameParser = new GogoParser(rem, rem.length());
            String name = cmdNameParser.value();
            name = session.resolveCommand(name);

            Parser cmdParser = null;
            for (Command cmd : session.getRegistry().getCommands()) {
                if (name.equals(cmd.getScope() + ":" + cmd.getName())) {
                    cmdParser = cmd.getParser();
                    break;
                }
            }
            if (cmdParser == null) {
                cmdParser = new DefaultParser();
            }

            CommandLine cmdLine = cmdParser.parse(session, rem, rem.length());
            for (int i = 0 ; i < cmdLine.getArguments().length; i++) {
                String arg = cmdLine.getArguments()[i];
                if (i > 0) {
                    parsed.append(" ");
                }
                parsed.append(arg);
            }

            int length = cmdLine.getBuffer().length();
            if (length < rem.length()) {
                char ch = rem.charAt(length);
                if (ch == ';' || ch == '|') {
                    parsed.append(" ");
                    parsed.append(ch);
                    parsed.append(" ");
                    length++;
                } else {
                    throw new IllegalArgumentException("Unrecognized character: '" + ch + "'");
                }
            }
            pos += length;
        }

        return parsed.toString();
    }

}