package Chat;

/*
Сетевой чат
-Ограниченное максимальное колличество клиентов.
-Сервер отслеживает запрещенные слова в сообщениях.  При обнаружении запрещенного слова в сообщении сервер
игнорирует сообщение (не рассылает его другим пользователем) и разорвает соединение с данным пользователем.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame implements Runnable {

    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final JTextArea outTextArea;
    private final JTextField inTextField;
    private final JButton button;
    private String name;

    public ChatClient(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        super("Client");
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;

        name = JOptionPane.showInputDialog(ChatClient.this, "Enter your name");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(name);
        setLayout(new BorderLayout());
        outTextArea = new JTextArea();
        add(outTextArea);
        JPanel jPanel = new JPanel();
        add(BorderLayout.SOUTH, jPanel);
        jPanel.setLayout(new BorderLayout());
        inTextField = new JTextField();
        jPanel.add(inTextField);
        button = new JButton("Send");
        jPanel.add(BorderLayout.EAST, button);
        button.addActionListener(e -> {
            try {
                dataOutputStream.writeUTF(name + ": " + inTextField.getText());
                dataOutputStream.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            inTextField.setText("");
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                ChatHandler.subtractCount();
                try {
                    dataOutputStream.close();
                    dataInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        inTextField.addActionListener(e -> {
            try {
                dataOutputStream.writeUTF(name + ": " + inTextField.getText());
                dataOutputStream.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            inTextField.setText("");
        });

        setVisible(true);
        inTextField.requestFocus();// что бы поле для ввода было активно
        new Thread(this).start();
    }


    public static void main(String[] args) {
        String site = "localhost";
        String port = "8082";

        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            socket = new Socket(site, Integer.parseInt(port));
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            new ChatClient(socket, dataInputStream, dataOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String line = dataInputStream.readUTF();
                outTextArea.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inTextField.setVisible(false);
            validate();
        }
    }
}
