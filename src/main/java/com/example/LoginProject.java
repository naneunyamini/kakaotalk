package com.example;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;
import javax.swing.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LoginProject {
    public static String getLogonID(){
        String logonID="";
        try{
            while(logonID.equals("")){
                //logonID = JOptionPane.showInputDialog("로그인 아이디를 입력하세요");
            }
        }catch(NullPointerException e){
            System.exit(0);
        }
        return logonID;
    }
   JPanel cardPanel;
   LoginProject lp;
   CardLayout card;
    
   

   public static void main(String[] args) {
      
    Connection conn;
    LoginProject lp = new LoginProject();
    lp.setFrame(lp);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // jdbc:mysql://IP:PORT/DBNAME
            String url = "jdbc:mysql://localhost:3306/toks?useSSL=false";
            conn = DriverManager.getConnection(url, "root", "1234");
            //디버깅 코드 **
            System.out.println("DB 연결 성공");
        } catch (SQLException e) {
            System.out.println("DB 연결 실패");
            System.err.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로딩 실패");
            System.err.println(e);
        }        
    }
	public void connectServer(String strServerIP) {
		int nServerPort = 2777;
        int nDelayTime = 10000;
        
        
        InetSocketAddress isaSvr= null;
        Socket socket = null;
        
        try {
           socket = new Socket();
           System.out.println("서버와 연결을 시도합니다...ing");
           isaSvr = new InetSocketAddress(strServerIP, nServerPort);
           socket.connect(isaSvr, nDelayTime);
           System.out.printf("연결 성공.[%s] [%d]\n", strServerIP, nServerPort);
        }catch(Exception e) {
           System.out.println(e);
        }
	}

   public void setFrame(LoginProject lpro) {

      JFrame jf = new JFrame();
      LoginPanel lp = new LoginPanel(lpro);
      signupPanel sp = new signupPanel(lpro);

      card = new CardLayout();

      cardPanel = new JPanel(card);
      cardPanel.add(lp.mainPanel, "Login");
      cardPanel.add(sp.mainPanel, "Register");
      
      jf.add(cardPanel);
      jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jf.setSize(500, 700);
      jf.setVisible(true);
   }

   public Connection getConnection() throws SQLException {
      Connection conn = null;
        
      conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/besteleven?useSSL=false", "root",
            "1234");

      return conn;
   }

}

class LoginPanel extends JPanel implements ActionListener {

   JPanel mainPanel;
   JTextField idTextField;
   JTextField ipTextField;
   JPasswordField passTextField;

   String userMode = "일반";
   LoginProject lp;
   Font font = new Font("회원가입", Font.BOLD, 40);
   String admin = "admin";

   public LoginPanel(LoginProject lp) {
      this.lp = lp;
      
      mainPanel = new JPanel();
      mainPanel.setLayout(new GridLayout(5, 1));

      JPanel centerPanel = new JPanel();
      JLabel loginLabel = new JLabel("로그인 화면");
      loginLabel.setFont(font);
      centerPanel.add(loginLabel);

      JPanel userPanel = new JPanel();

      JPanel gridBagidInfo = new JPanel(new GridBagLayout());
      gridBagidInfo.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
      GridBagConstraints c = new GridBagConstraints();

   

      JLabel idLabel = new JLabel("                아이디 : ");
      c.weightx = 0.5; // flowLayout
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 0;
      gridBagidInfo.add(idLabel, c);

      idTextField = new JTextField(30);
      c.weightx = 0.5;
      c.insets = new Insets(0, 5, 0, 0);
      c.gridx = 1;
      c.gridy = 0;
      gridBagidInfo.add(idTextField, c);

      JLabel passLabel = new JLabel("                비밀번호 : ");
      c.weightx = 0.5;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 1;
      c.insets = new Insets(20, 0, 0, 0);
      gridBagidInfo.add(passLabel, c);

      passTextField = new JPasswordField(15);
      c.weightx = 0.5;
      c.insets = new Insets(20, 5, 0, 0);
      c.gridx = 1;
      c.gridy = 1;
      gridBagidInfo.add(passTextField, c);

      JLabel ipLabel = new JLabel("                서버 IP : ");
      c.weightx = 0.5;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 2;
      c.insets = new Insets(20, 0, 0, 0);
      gridBagidInfo.add(ipLabel, c);

      ipTextField = new JTextField(15);
      c.weightx = 0.5;
      c.insets = new Insets(20, 5, 0, 0);
      c.gridx = 1;
      c.gridy = 2;
      gridBagidInfo.add(ipTextField, c);

      JPanel loginPanel = new JPanel();
      JButton loginButton = new JButton("로그인");
      loginPanel.add(loginButton);

      JPanel signupPanel = new JPanel();
      JButton signupButton = new JButton("회원가입");
      loginPanel.add(signupButton);

      mainPanel.add(centerPanel);
      mainPanel.add(userPanel);
      mainPanel.add(gridBagidInfo);
      mainPanel.add(loginPanel);
      mainPanel.add(signupPanel);


      loginButton.addActionListener(this);

      signupButton.addActionListener(new ActionListener() {
         // TODO Auto-generated method stub
         @Override
         public void actionPerformed(ActionEvent e) {
            lp.card.next(lp.cardPanel);
         }
      });

   }

   @Override
   public void actionPerformed(ActionEvent e) {
      JButton jb = (JButton) e.getSource();

      switch (e.getActionCommand()) {
      case "일반":
         userMode = "일반";
         break;

      case "관리자":
         userMode = "관리자";
         break;

      case "로그인":

         String id = idTextField.getText();
         String password = new String(passTextField.getPassword());
         String strServerIP = ipTextField.getText();
         
         try {

            String sql_query = String.format("SELECT password FROM user_info WHERE id= '%s' AND password='%s'",id, password); //SELECT * FROM user_info LIMIT 100;
            Connection conn = lp.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(sql_query);
            System.out.println(rset);
            rset.next();
			lp.connectServer(strServerIP);
            if (password.equals(rset.getString(1))) {
               
               JOptionPane.showMessageDialog(this, "Login Success", "로그인 성공", 1);

               ClientThread thread = new ClientThread();
                   thread.start();
                   thread.requestLogon(id);
               
            } else
               JOptionPane.showMessageDialog(this, "Login Failed", "로그인 실패", 1);

         } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login Failed", "로그인 실패", 1);
            System.out.println("SQLException" + ex);
         }

         break;

      }
   }

} // class LoginPanel

class signupPanel extends JPanel {

   JTextField idTf;
   JPasswordField passTf;
   JTextField nameTf;
   JTextField phoneTf;
   JPanel mainPanel;
   JPanel subPanel;
   JButton registerButton;
   Font font = new Font("회원가입", Font.BOLD, 40);

   String id = "", password = "", name = "";
   LoginProject lp;

   public signupPanel(LoginProject lp) {

      this.lp = lp;
      subPanel = new JPanel();
      subPanel.setLayout(new GridBagLayout());
      subPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

      JLabel idLabel = new JLabel("아이디 : ");
      JLabel passLabel = new JLabel("비밀번호 : ");
      JLabel nameLabel = new JLabel("이름 : ");
      

      idTf = new JTextField(15);
      passTf = new JPasswordField(15);
      nameTf = new JTextField(15);



      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(15, 5, 0, 0);

      c.gridx = 0;
      c.gridy = 0;
      subPanel.add(idLabel, c);

      c.gridx = 1;
      c.gridy = 0;
      subPanel.add(idTf, c); // 아이디

      c.gridx = 0;
      c.gridy = 1;
      subPanel.add(passLabel, c);

      c.gridx = 1;
      c.gridy = 1;
      subPanel.add(passTf, c); // pass
      
      c.gridx = 2;
      c.gridy = 1; 
      subPanel.add(new JLabel("특수문자 + 8자"),c); //보안설정


      c.gridx = 0;
      c.gridy = 3;
      subPanel.add(nameLabel, c);

      c.gridx = 1;
      c.gridy = 3;
      subPanel.add(nameTf, c); // 이름


      mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      JLabel signupLabel = new JLabel("회원가입 화면 ");
      signupLabel.setFont(font);
      signupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

      registerButton = new JButton("회원가입");
      registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

      mainPanel.add(signupLabel);
      mainPanel.add(subPanel);
      mainPanel.add(registerButton);

      registerButton.addActionListener(new ActionListener() {      //회원가입버튼

         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            id = idTf.getText();
            password = new String(passTf.getPassword());
            name = nameTf.getText();
            

            String sql = "INSERT INTO user_info(id, password, name) values (?, ?, ?)";

            Pattern passPattern1 = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,20}$"); //영문+특문+숫자 포함 8자 이상
            Matcher passMatcher = passPattern1.matcher(password);

            if (!passMatcher.find()) {
               JOptionPane.showMessageDialog(null, "비밀번호는 영문+특수문자+숫자 8자이상 구성되어야 합니다", "비밀번호 오류", 1);
            }else {
               try {
                  Connection conn = lp.getConnection();

                  PreparedStatement pstmt = conn.prepareStatement(sql);

                  pstmt.setString(1, idTf.getText());
                  pstmt.setString(2, password);
                  pstmt.setString(3, nameTf.getText());

                  int r = pstmt.executeUpdate();
                  System.out.println("변경된 row " + r);
                  JOptionPane.showMessageDialog(null, "회원 가입 완료!", "회원가입", 1);
                  lp.card.previous(lp.cardPanel); // 다 완료되면 로그인 화면으로
               } catch (SQLException e1) {
                  System.out.println("SQL error" + e1.getMessage());
                  if (e1.getMessage().contains("PRIMARY")) {
                     JOptionPane.showMessageDialog(null, "아이디 중복!", "아이디 중복 오류", 1);
                  } else
                     JOptionPane.showMessageDialog(null, "정보를 제대로 입력해주세요!", "오류", 1);
               } // try ,catch
            }
         }
      });

   }
}