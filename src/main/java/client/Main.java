package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static final int PORT = 34522;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    public static void main(String[] args) {
        // todo - your java-code
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            //System.out.println("Выберите действие, которое хотите произвести --> 1-отрпавить файл на сервер\n 2-достать файл из сервера \n 3-удалить файл из сервера");
            //int action = scanner.nextInt();
            sendFile("C:\\Users\\fikus\\IdeaProjects\\se-lab04-tmp2223\\src\\main\\java\\client\\data\\1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void sendFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        // отправляем через сокет длину файла
        outputStream.writeLong(file.length());
        // тут массив может быть очень большой, необходимо будет это учитывать потом
        byte[] buffer = new byte[(int) file.length()];
        // в переменнную буфер считываем все байты из файла
        fileInputStream.read(buffer);
        for(int i =0; i<buffer.length; i++) {
            System.out.println(buffer[i]);
        }
        // отправили через сокет
        outputStream.write(buffer);
    }

}
