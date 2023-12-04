package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName); 
        sortedCharFreqList = new ArrayList<>();
        int[] arr = new int[128]; 
        double size = 0;   
        String bruh = "";
         while(StdIn.hasNextChar()){ 
            int ind = StdIn.readChar();
            arr[ind]++;
            size++;   
            if(arr[ind]==1){ 
                bruh += (char)ind;
            } 
        }   
        for(int x = 0 ;x<bruh.length();x++){
            sortedCharFreqList.add(new CharFreq(bruh.charAt(x),arr[bruh.charAt(x)]/size));
        }
        if(sortedCharFreqList.size()==1){ 
            sortedCharFreqList.add(new CharFreq((char)(sortedCharFreqList.get(0).getCharacter()+1), 0.0));
        } 
        Collections.sort(sortedCharFreqList); 

	/* Your code goes here */
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        Queue<TreeNode> target = new Queue<>();   
        Queue<TreeNode> src = new Queue<>(); 
        while(sortedCharFreqList.size()!=0){
            src.enqueue(new TreeNode(sortedCharFreqList.remove(0) , null, null));
        } 
        while(!src.isEmpty()|| target.size()!=1){ 
         TreeNode left; 
         TreeNode right;
            if(target.size()==0){
                left = src.dequeue(); 
                right = src.dequeue();
            } 
            else if(src.size()==0){ 
                left = target.dequeue(); 
                right = target.dequeue();
            } 
            else{ 
                if(target.peek().getData().getProbOcc()>=src.peek().getData().getProbOcc()){
                    left = src.dequeue();   
                } 
                else { 
                    left = target.dequeue(); 
                }  
                if(src.size()==0){
                    right  = target.dequeue();
                } 
                else if ( target.size()==0){
                    right  = src.dequeue();
                }
                else if(target.peek().getData().getProbOcc()>=src.peek().getData().getProbOcc()){
                    right = src.dequeue();   
                } 
                else { 
                    right = target.dequeue(); 
                } 
            }  
           target.enqueue(new TreeNode(new CharFreq(null, left.getData().getProbOcc() + right.getData().getProbOcc()), left, right));
        } 
        huffmanRoot = target.peek();
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128]; 
        makeEncodings("", huffmanRoot);
        }
    private void makeEncodings(String num, TreeNode root){   
        if(root.getLeft() ==null && root.getRight()==null){
            encodings[(int)root.getData().getCharacter()] = num; 
            return;
         }  

         makeEncodings(num+ "0",root.getLeft());  
         makeEncodings(num+"1",root.getRight()); 
	/* Your code goes here */
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);   
        String something = "";
        while(StdIn.hasNextChar()){
            something+= encodings[(int)StdIn.readChar()];
        } 
        writeBitString(encodedFile, something);

	/* Your code goes here */
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);  
        String decoded = "";
         String word = readBitString(encodedFile);  
         TreeNode ptr = getHuffmanRoot(); 
         int count = 0;
        while(count<word.length()){ 
            
            if(word.charAt(count)=='0'){
                ptr = ptr.getLeft(); 
                count++;
            } 
            else{
                ptr = ptr.getRight(); 
                count++;
            } 
            if (ptr.getLeft()==null && ptr.getRight()==null){
                decoded += ptr.getData().getCharacter(); 
                ptr = huffmanRoot; 
            }
        } 
        StdOut.print(decoded);
	/* Your code goes here */
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
