package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {


    static JFrame chatWindow = new JFrame("Chat Application");
    static JTextArea chatArea = new JTextArea(22, 40);
    static JTextField textField = new JTextField(40);
    static JLabel blankLabel = new JLabel("                ");
    static JButton sendButton = new JButton("Send");
    static JButton FileUploadButton = new JButton("FileUpload");
    static JButton FileDownloadButton = new JButton("FileDownload    ");
    static JLabel namelabel = new JLabel("            ");

    static BufferedReader in;
    static PrintWriter out;


    private static String clientName = null;
    public static String getClientName() {
        return clientName;
    }


    public Client() {

        chatWindow.setLayout(new FlowLayout());
        chatWindow.add(namelabel);
        chatWindow.add(new JScrollPane(chatArea));
        chatWindow.add(blankLabel);
        chatWindow.add(textField);
        chatWindow.add(sendButton);

        chatWindow.setSize(475, 500);
        chatWindow.setResizable(false);
        chatWindow.setVisible(true);
        textField.setEditable(false);
        chatArea.setEditable(false);
        chatWindow.setResizable(false);

        sendButton.addActionListener(new Listener());
        textField.addActionListener(new Listener());

        chatWindow.addWindowListener(new WindowAdapter() {
            // Invoked when a window has been opened.
/*
            public void windowOpened(WindowEvent e) {
                System.out.println("Window Opened Event");
            }
*/

            // Invoked when a window is in the process of being closed.
            // The close operation can be overridden at this point.
            public void windowClosing(WindowEvent e) {
                exitProgram();

            }

/*
            // Invoked when a window has been closed.
            public void windowClosed(WindowEvent e) {
                System.out.println("Window Close Event");
            }
*/
        });

    }

    public void exitProgram() {
        if (out != null)
            out.println("LEAVING:" + clientName);

        System.out.println(clientName+ " is LEAVING" );
        chatWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.exit(0);
    }


    public void startChat() throws IOException {
        Socket soc = null;
        while (soc == null) {
            String ipAddress = JOptionPane.showInputDialog(chatWindow,
                    "Enter IP Address",
                    "IP Address Required !!",
                    JOptionPane.PLAIN_MESSAGE);

            if (ipAddress == null) {
                chatWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                System.exit(0);
            } else if (ipAddress.trim().length() == 0)
                continue;
            else {
                try {
                    soc = new Socket(ipAddress, 9806);
                } catch (ConnectException e) {
                    JOptionPane.showMessageDialog(chatWindow, "Server is not available");
                    System.exit(0);
                } catch (UnknownHostException e) {
                    JOptionPane.showMessageDialog(chatWindow, "Please enter a valid IP address");
                    continue;
                }
            }
        }

        in = new BufferedReader(new InputStreamReader(soc.getInputStream())); // stdin
        out = new PrintWriter(soc.getOutputStream(), true);   // os

        while (true) {

            String str = in.readLine();

            if (str == null)
                return;

            if (str.equals("NAME_REQUIRED")) {

                String name = null;

                do {
                    name = enterUserName(chatWindow, "Name Required!! - Enter a unique name:");
                } while (name == null || name.trim().length() == 0);
                out.println(name);

            } else if (str.equals("NAME_ALREADY_EXISTS")) {
                String name = null;
                do {
                    name = enterUserName(chatWindow, "Name already Exists!! - Enter another name:");
                } while (name == null);
                out.println(name);
            } else if (str.startsWith("NAME_ACCEPTED:")) {
                textField.setEditable(true);
                clientName = str.substring(14);
                namelabel.setText("Logged in as : " + clientName);
            } else {
                chatArea.append(str + "\n");
            }

        }

    }

    private String enterUserName(JFrame jFrame, String message) {
        String[] options = {"OK"};
        JPanel panel = new JPanel();
        JLabel lbl = new JLabel("Enter Your name: ");
        JTextField txt = new JTextField(10);
        panel.add(lbl);
        panel.add(txt);

        int selectedOption = JOptionPane.showOptionDialog(jFrame, panel, message, JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selectedOption == 0) {
            String text = txt.getText().trim();
            if (text.length() > 0)
                return text;
        }

        return null;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.startChat();
    }

}


class Listener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String messageTosend = Client.textField.getText().trim();

        if (messageTosend.trim().length() > 0) {

            if (messageTosend.startsWith("exit")) {
                Client.out.println("exit:" + Client.getClientName());
                Client.chatWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                System.exit(0);
            }

            Client.out.println(messageTosend);
            Client.textField.setText("");

        }
    }
}