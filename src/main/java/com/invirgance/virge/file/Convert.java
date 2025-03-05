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


import com.invirgance.convirgance.ConvirganceException;
import com.invirgance.convirgance.input.CSVInput;
import com.invirgance.convirgance.input.DelimitedInput;
import com.invirgance.convirgance.input.Input;
import com.invirgance.convirgance.input.JBINInput;
import com.invirgance.convirgance.input.JSONInput;
import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.output.CSVOutput;
import com.invirgance.convirgance.output.DelimitedOutput;
import com.invirgance.convirgance.output.JBINOutput;
import com.invirgance.convirgance.output.JSONOutput;
import com.invirgance.convirgance.output.Output;
import com.invirgance.convirgance.source.FileSource;
import com.invirgance.convirgance.source.InputStreamSource;
import com.invirgance.convirgance.source.Source;
import com.invirgance.convirgance.target.FileTarget;
import com.invirgance.convirgance.target.OutputStreamTarget;
import com.invirgance.convirgance.target.Target;
import com.invirgance.convirgance.transform.CoerceStringsTransformer;
import com.invirgance.virge.Virge;
import static com.invirgance.virge.Virge.exit;
import static com.invirgance.virge.file.VirgeFile.HELP_DESCRIPTION_SPACING;
import static com.invirgance.virge.file.VirgeFile.HELP_SPACING;
import static com.invirgance.virge.file.VirgeFile.printToolHelp;
import com.invirgance.virge.tool.Tool;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;


/**
 * For converting from one file into another, ex JSON -> CSV
 * @author jbanes
 */
public class Convert implements Tool
{
    private Source source;
    private Input<JSONObject> input;

    private Target target;
    private Output output;
    
    private char inputDelimiter;
    private char outputDelimiter;
    private boolean jbinCompress;
    private boolean detectTypes;

    public Source getSource()
    {
        return source;
    }

    public void setSource(Source source)
    {
        this.source = source;
    }

    public Input<JSONObject> getInput()
    {
        return input;
    }

    public void setInput(Input<JSONObject> input)
    {
        this.input = input;
    }

    public Target getTarget()
    {
        return target;
    }

    public void setTarget(Target target)
    {
        this.target = target;
    }

    public Output getOutput()
    {
        return output;
    }

    public void setOutput(Output output)
    {
        this.output = output;
    }
    
    private boolean isURL(String path)
    {
        char c;
        
        if(!path.contains(":/")) return false;
        if(path.charAt(0) == ':') return false;
        
        for(int i=0; i<path.length(); i++)
        {
            c = path.charAt(i);
            
            if(c == ':') return (path.charAt(i+1) == '/');
                
            if(!Character.isLetter(c)) return false;
        }
        
        return false;
    }
    
    private Source getSource(String path) throws MalformedURLException, IOException
    {
        File file;
        
        if(path.equals("-")) return new InputStreamSource(System.in);
          
        if(isURL(path))
        {
            return new InputStreamSource(URI.create(path).toURL().openStream());
        }
        
        file = new File(path);
        
        if(!file.isFile()){
            System.err.println("Invalid File Source: " + file.toString());
            throw new ConvirganceException("File not found: " + path);
        }
        
        return new FileSource(file);
    }
    
    // TODO: Improve auto-detection
    private Input<JSONObject> detectInput(String path) throws MalformedURLException
    {
        if(isURL(path))
        {
            path = URI.create(path).toURL().getFile();
        }
        
        path = path.toLowerCase();
        
        if(path.endsWith(".json")) return new JSONInput();
        if(path.endsWith(".csv")) return new CSVInput(); 
        if(path.endsWith(".jbin")) return new JBINInput();
        
        return null;
    }
    
    private Input<JSONObject> getInputType(String type)
    {
        switch(type)
        {
            case "csv": 
                return new CSVInput();
            
            case "tsv":
                return new DelimitedInput('\t');
            
            case "pipe":
                return new DelimitedInput('|');
            
            case "delimited":
                
                if(inputDelimiter != 0) return new DelimitedInput(inputDelimiter);
                
                return new DelimitedInput();
            
            case "jbin":
                return new JBINInput();
                
            case "json":
                return new JSONInput();
                
            default:
                exit(255, "Unknown input type: " + type);
                return null; // Keep the compiler happy
        }
    }
    
    private Target getTarget(String path) throws MalformedURLException, IOException
    {
        File file;

        if(path.equals("-")) return new OutputStreamTarget(System.out);
    
        if(isURL(path))
        {
            return new OutputStreamTarget(URI.create(path).toURL().openConnection().getOutputStream());
        }
        
        file = new File(path);
        
        if(file.getParentFile() != null && !file.getParentFile().exists()) 
        {
            file.getParentFile().mkdirs();
        }
        
        return new FileTarget(file);
    }
    
    // TODO: Should the default be symetrical input/output?
    private Output detectOutput(String path) throws MalformedURLException
    {
        if(isURL(path))
        {
            path = URI.create(path).toURL().getFile();
        }
        
        path = path.toLowerCase();
        
        if(path.endsWith(".json")) return new JSONOutput();
        if(path.endsWith(".csv")) return new CSVOutput(); 
        if(path.endsWith(".jbin")) return new JBINOutput(jbinCompress);
        
        return null;
    }
    
    private Output getOutput(String type)
    {
        switch(type)
        {
            case "csv": 
                return new CSVOutput();
            
            case "tsv":
                return new DelimitedOutput('\t');
            
            case "pipe":
                return new DelimitedOutput('|');
            
            case "delimited":
                
                if(outputDelimiter != 0) return new DelimitedOutput(outputDelimiter);
                
                return new DelimitedOutput();
            
            case "jbin":
                return new JBINOutput(jbinCompress);
                
            case "json":
                return new JSONOutput();
                
            default:
                exit(255, "Unknown output type: " + type);
                return null; // Keep the compiler happy
        }
    }

    private boolean error(String message)
    {
        System.err.println(message);
        
        return false;
    }
    
    @Override
    public String getShortDescription()
    {
        return "Transforms a file from its original format to the provided one.";
    }    
    
    @Override
    public String getName()
    {
        return "convert";
    }
       
    @Override
    public String[] getHelp()
    {
        return new String[]
        {
            HELP_SPACING + "--source <\"file path\"> or piped data <\"-\">",
            HELP_SPACING + "-s <\"file path\"> or piped data <\"-\">",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Alternate method of specifying the source file. When piping data the input type must be specified.",
            "",
            HELP_SPACING + "--source-type <format>",
            HELP_SPACING + "-i <format>",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Specify the format of the source file. Currently supported options are json, csv, tsv, pipe, delimited, and jbin",
            "",
            HELP_SPACING + "--target <\"file path\"> or piped out <\"-\">",
            HELP_SPACING + "-t <\"file path\"> or piped out <\"-\">",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Alternate method of specifying the target file. When piping data out the target type must be specified.",
            "",
            HELP_SPACING + "--target-type <format>",
            HELP_SPACING + "-o <format>",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Specify the format of the target file. Currently supported options are json, csv, tsv, pipe, delimited, and jbin",
            "",
            HELP_SPACING + "--jbin-compress",
            HELP_SPACING + "-z",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Enable compression when writing a jbin file",
            "",
            HELP_SPACING + "--source-delimiter <delimiter>",
            HELP_SPACING + "-D <delimiter>",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Set the column delimiter if the source is a delimited file (e.g. , or |)",
            "",
            HELP_SPACING + "--target-delimiter <delimiter>",
            HELP_SPACING + "-d <delimiter>",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Set the column delimiter if the target is a delimited file (e.g. , or |)",
            "",
            HELP_SPACING + "--detect-input-types",
            HELP_SPACING + "-T",
            HELP_SPACING + HELP_DESCRIPTION_SPACING + "Attempts to automatically coerce strings in the input records into numbers and booleans.",
        };
    }
    
    @Override
    public boolean parse(String[] args, int start) throws MalformedURLException, IOException
    {
        if(start == args.length) 
        {
            printToolHelp(this);
            
            return true;
        }
        
        for(int i=start; i<args.length; i++)
        {
            // Handle single-letter params with no spaces in them
            if(args[i].length() > 2 && args[i].charAt(0) == '-' && Character.isLetterOrDigit(args[i].charAt(1)))
            {
                parse(new String[]{ args[i].substring(0, 2), args[i].substring(2) }, 0);
                
                continue;
            }
            
            switch(args[i])
            {
                case "--help":
                case "-h":
                case "-?":
                    printToolHelp(this);
                    return true;
                    
                case "--jbin-compress":
                case "-z":
                    jbinCompress = true;
                    
                    if(output instanceof JBINOutput) ((JBINOutput)output).setCompressed(jbinCompress);
                    
                    break;
                
                case "--source-delimiter":
                case "-D":
                    inputDelimiter = args[++i].charAt(0);
                    
                    if(input instanceof DelimitedInput) ((DelimitedInput)input).setDelimiter(inputDelimiter);
                    
                    break;
                    
                case "--target-delimiter":
                case "-d":
                    outputDelimiter = args[++i].charAt(0);
                    
                    if(output instanceof DelimitedOutput) ((DelimitedOutput)output).setDelimiter(outputDelimiter);
                    
                    break;
                
                case "--source":
                case "-s":
                    source = getSource(args[++i]);
                    
                    if(input == null) input = detectInput(args[i]);
                    
                    break;
                    
                case "--source-type":
                case "-i":
                    input = getInputType(args[++i]);
                    break;
                    
                case "--target":
                case "-t":
                    target = getTarget(args[++i]);
                    
                    if(output == null) output = detectOutput(args[i]);
                    
                    break;
                    
                case "--target-type":
                case "-o":
                    output = getOutput(args[++i]);
                    break;
                    
                case "--detect-input-types":
                case "-T":
                    detectTypes = true;
                    break;
                    
                default:
                    
                    if(source == null && (args[i].equals("-") || args[i].contains(".")))
                    {
                        source = getSource(args[i]);
                    
                        if(input == null) input = detectInput(args[i]);

                        break;
                    }
                    else if(target == null && (args[i].equals("-") || args[i].contains(".")))
                    {
                        target = getTarget(args[i]);

                        if(output == null) output = detectOutput(args[i]);

                        break;
                    }
                    else
                    {
                        exit(255, "Unknown parameter: " + args[i]);
                    }
            }
        }
        
        if(source == null) return error("No source specified!");
        if(input == null) return error("No source type specified and unable to autodetect");
        if(target == null) return error("No target specified!");
        if(output == null) return error("No target type specified and unable to autodetect");
        
        return true;
    }

    @Override
    public void execute()
    {
        Iterable<JSONObject> iterable;
        
        if(source == null) Virge.exit(254, "No source specified!");
        if(input == null) Virge.exit(254, "No input type specified and unable to autodetect");
        if(target == null) Virge.exit(254, "No target specified!");
        if(output == null) Virge.exit(254, "No output type specified and unable to autodetect");
        
        iterable = input.read(source);
        
        if(detectTypes) iterable = new CoerceStringsTransformer().transform(iterable);
        
        output.write(target, iterable);
    }
}
