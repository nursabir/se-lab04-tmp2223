package server;

import java.io.*;
import java.net.Socket;

public class Session extends Thread {
    //
    private final Socket socket;
    static DataOutputStream outputStream;
    static DataInputStream inputStream;

    public Session(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {

    }

    // из соекетового потока получаем массив байтов
    private static byte[] getArrayOfBytesFromSocketStream() throws IOException {
        int length = (int) inputStream.readLong();
        byte[] resultBytes = new byte[length];
        inputStream.readFully(resultBytes, 0, length);
        return resultBytes;
    }

    // по заданному имени создает файл в data и байты из массива записывает в новый файл
    private static void writeBytesInNewFile(String nameOfFile, byte[] bytes) throws FileNotFoundException {
        String pathToNewFile = "C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\server\\data\\" + nameOfFile;
        File file = new File(pathToNewFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
