package gr.aueb.straboio.keyboard;

public class InputBuffer {
    private InputConverter iconv;
    private String buffer = "";
    private String newbuffer = "";

    public InputBuffer(InputConverter ic){
        this.iconv = ic;
    }

    public void push(String str){
        buffer += str;
    }

    public void push(char c){
        buffer += String.valueOf(c);
    }

    public void trigger() {
        newbuffer = this.iconv.convert(buffer);
    }

    public int bufferLength(){
        return buffer.length();
    }

    public int convertedBufferLength(){
        return newbuffer.length();
    }

    public void clear(){
        buffer = "";
        newbuffer = "";
    }

    public String newbuffer() {
        return newbuffer;
    }
}
