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

import static com.invirgance.virge.Virge.HELP_DESCRIPTION_SPACING;
import static com.invirgance.virge.Virge.HELP_SPACING;
import com.invirgance.virge.tool.Tool;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This serves as the Virge file module, for copying, converting or acting on files in some way.
 *
 * @author tadghh
 */
public class VirgeFile 
{
    
    private static final String HELP = "Tools for copying, converting/transforming files.";
    
    public static Tool SELECTED;

    public static final Map<String,Tool> lookup = new HashMap<>();
    
    public static final Tool[] tools = new Tool[] {
        new Convert(),
    }; 
    
    static {
        for(Tool tool : tools) lookup.put(tool.getName(), tool);
    }
    
    public static void print(String[] lines, PrintStream out)
    {
        for(String line : lines)
        {
            out.println(line);
        }
        
        out.println();
        out.println();
    }
    
    public static void printToolHelp(Tool selected)
    {
        // TODO look at adding sub tools to Tool
        Boolean level = SELECTED != null && selected != null && !SELECTED.getName().equals(selected.getName()) || SELECTED != null && selected == null;
        
        String top = level ? SELECTED.getName() : "";
        String sub = selected != null ? selected.getName() : "";
        
        // This is for tools that have no sub tools but are parents (declared in tools above)
        String singleTool = level ?  " " : "";   
        
        // Tools without commands
        if(selected != null)
        {
            System.out.println();
            System.out.println("Usage: virge.jar file " + top + singleTool + sub + " <OPTIONS>");
            System.out.println();
            System.out.println(selected.getShortDescription());
            System.out.println();
            System.out.println("Options:");
            System.out.println();
            
            print(selected.getHelp(), System.out);
        }
        else if(SELECTED != null)
        {
            // Top level tools
            System.out.println();
            System.out.println("Usage: virge.jar file " + top + sub + " <COMMAND>");
            System.out.println();
            System.out.println(SELECTED.getShortDescription());
            System.out.println();            
            System.out.println("Options:");
            System.out.println();
            System.out.println(HELP_SPACING + "--help");
            System.out.println(HELP_SPACING + "-h");
            System.out.println(HELP_SPACING + HELP_DESCRIPTION_SPACING + "View a tools options.");
            System.out.println();
            System.out.println("Commands:");
            System.out.println();
            
            print(SELECTED.getHelp(), System.out); 
        }
        else
        {
            // No command
            System.out.println();
            System.out.println("Usage: virge.jar file <TOOL>");
            System.out.println();
            System.out.println(HELP);
            System.out.println();
            System.out.println("Tools:");
            System.out.println();

            for(Tool help : tools)
            {
                System.out.println(HELP_SPACING + help.getName() + " - " + help.getShortDescription());
            }  

            System.out.println(); 
        }
        
        System.exit(1);
    }
    
    /**
     * The entry point for this module, the TLC (top level command) should be stripped before this is called
     * @param args The commands for the virge-file module
     * @throws Exception Possible IO exceptions for target or source (bad path)
     */
    public static void main(String[] args) throws Exception
    {
        // NOTE: -? might be a special pattern in some shells, zsh?
        if(args.length == 0 || args[0].equals("--help") || args[0].equals("-h"))
        {   
            printToolHelp(null);
         
            return;
        }
        
        SELECTED = lookup.get(args[0]);

        if(SELECTED == null) 
        {
            System.err.println("\nUnknown Command: " + args[0]);
            
            printToolHelp(null);
        }
        
        if(!SELECTED.parse(args, 1)) printToolHelp(null);
        
        SELECTED.execute();
    }
}
