/*
 * Copyright 2024 INVIRGANCE LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.invirgance.virge.file;

import com.invirgance.virge.tool.Tool;
import java.io.PrintStream;

/**
 * This serves as the Virge File module, for copying, converting or acting on files in some way.
 * @author tadghh
 */
public class VirgeFile {
    private static Tool tool = new Convert();

    public static void main(String[] args) throws Exception 
    {
        if(args.length < 1) printShortHelp();
        
        if(args[0].equals("--help") || args[0].equals("-h") || args[0].equals("-?"))
        {
            printShortHelp();
        }
  
        if(tool == null) exit(6, "Unknown tool: " + args[0]);
        
        // Test this
        if(!tool.parse(args, 1)) print(tool.getHelp(), System.out);
        
        tool.execute();
    }
    
    private static void print(String[] lines, PrintStream out)
    {
        for(String line : lines)
        {
            out.println(line);
        }
        
        out.println();
        out.println();
    }
    
    public static void exit(int code, String message)
    {
        System.err.println(message);
        
        System.exit(code);
    }
    
    public static void printShortHelp()
    {
        
        System.out.println();
        System.out.println("Usage: java -jar virge-file.jar file [options] <source> <target>");
        System.out.println();
        System.out.println("Commands:");
        System.out.println();
        
        System.out.println("    " + tool.getHelp()[0]);
        
        System.out.println();
        System.exit(1);
    }
    
}
