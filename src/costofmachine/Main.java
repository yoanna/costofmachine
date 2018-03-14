package costofmachine;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.toedter.calendar.JDateChooser;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblZakresDatOd = new JLabel("Zakres dat od");
		
		JLabel lblZakresDatDo = new JLabel("Zakres dat do");
		
		JDateChooser dateChooser = new JDateChooser();
		JDateChooser dateChooser_1 = new JDateChooser();
		
		JButton btnNewButton = new JButton("Start analizy");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//countCost.run(from, to, path);
				Date data_od = dateChooser.getDate();
				Date data_do = dateChooser_1.getDate();
				if(data_od == null || data_do==null) {
					String data_od_tmp; String data_do_tmp;
					if(data_od == null) {
						data_od_tmp = "";
					}
					else data_od_tmp = format.format(data_od);
					if(data_do == null) {
						data_do_tmp = format.format(Calendar.getInstance().getTime());
					}
					else
						data_do_tmp = format.format(data_do);
					countCost.run(data_od_tmp, data_do_tmp);
				}
				else {
					if(data_od.compareTo(data_do)>0){
						//jezeli data od jest pozniej niz data do
						JOptionPane.showMessageDialog(null, "Data pocz¹tku zakresu niepoprawna");
						dateChooser.setDate(null);
						
					}
					else {
						countCost.run(format.format(data_od), format.format(data_do));
					}
				}
				
			}
		});
		
		JLabel lblPlikTekstowyZ = new JLabel("<html> Plik tekstowy z wynikiem analizy zostanie zapisany na dysku sieciowym //dataserver/Logistyka/Analiza maszyn </html>");
		lblPlikTekstowyZ.setHorizontalAlignment(SwingConstants.CENTER);
		lblPlikTekstowyZ.setToolTipText("");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(96)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblZakresDatOd)
								.addComponent(lblZakresDatDo))
							.addGap(75)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(dateChooser_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(dateChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(165)
							.addComponent(btnNewButton))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblPlikTekstowyZ, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(57)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblZakresDatOd)
						.addComponent(dateChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(32)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblZakresDatDo)
							.addGap(6))
						.addComponent(dateChooser_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(26)
					.addComponent(btnNewButton)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblPlikTekstowyZ, GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
					.addContainerGap())
		);
		contentPane.setLayout(gl_contentPane);
	}
}
