package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import net.librec.data.model.ArffAttribute;
import net.librec.data.model.ArffInstance;

public class Util {
	
	/**
     * Read data from the data file.
     *
     * @throws IOException
     *         if the path is not valid
     */
    public static ArrayList<ArffInstance> readData(String dataPath) throws IOException {
    	ArrayList<ArffInstance> instances = new ArrayList<>();
        ArrayList<ArffAttribute> attributes = new ArrayList<>();
        ArrayList<String> attrTypes = new ArrayList<>();
        
        BufferedReader br = new BufferedReader(new FileReader(dataPath));
        boolean dataFlag = false;

        int attrIdx = 0;

        String attrName = null;
        String attrType = null;
        String line = null;

        while (true) {

            // parse DATA if valid
            if (dataFlag) {
                // get all attribute types
                for (ArffAttribute attr : attributes) {
                    attrTypes.add(attr.getType());
                }
                // let data reader control the bufferedReader
                dataReader(br, attrTypes, instances);
            }

            line = br.readLine();

            if (line == null) // finish reading
                break;
            if (line.isEmpty() || line.startsWith("%")) // skip empty or
                // annotation
                continue;

            String[] data = line.trim().split("[ \t]");

            // parse ATTRIBUTE
            if (data[0].toUpperCase().equals("@ATTRIBUTE")) {
                attrName = data[1];
                attrType = data[2];
                attributes.add(new ArffAttribute(attrName, attrType.toUpperCase(), attrIdx++));
            }
            // set DATA flag (finish reading ATTRIBUTES)
            else if (data[0].toUpperCase().equals("@DATA")) {
                dataFlag = true;
            }
        }
        br.close();
        
        // initialize instance attributes
        ArffInstance.attrs = attributes;
        
        return instances;
    }
    
    /**
     * Parse @DATA part of the file.
     *
     * @param rd  the reader of the input file.
     * @throws IOException
     */
    public static void dataReader(Reader rd, ArrayList<String> attrTypes, ArrayList<ArffInstance> instances) throws IOException {
        ArrayList<String> dataLine = new ArrayList<>();
        StringBuilder subString = new StringBuilder();
        boolean isInQuote = false;
        boolean isInBracket = false;

        int c = 0;
        while ((c = rd.read()) != -1) {
            char ch = (char) c;
            // read line by line
            if (ch == '\n') {
                if (dataLine.size() != 0) { // check if empty line
                    if (!dataLine.get(0).startsWith("%")) { // check if
                        // annotation line
                        dataLine.add(subString.toString());
                        // raise error if inconsistent with attribute define
                        if (dataLine.size() < attrTypes.size()) {
                            throw new IOException("Read data error, inconsistent attribute number!");
                        }

                        // pul column value into columnIds, for one-hot encoding
                        for (int i = 0; i < attrTypes.size(); i++) {
                            String col = dataLine.get(i).trim();
                            dataLine.set(i, col);
                        }

                        instances.add(new ArffInstance(dataLine));

                        subString = new StringBuilder();
                        dataLine = new ArrayList<>();
                    }
                }
            } else if (ch == '[' || ch == ']') {
                isInBracket = !isInBracket;
            } else if (ch == '\r') {
                // skip '\r'
            } else if (ch == '\"') {
                isInQuote = !isInQuote;
            } else if (ch == ',' && (!isInQuote && !isInBracket)) {
                dataLine.add(subString.toString());
                subString = new StringBuilder();
            } else {
                subString.append(ch);
            }
        }
    }
    
}
