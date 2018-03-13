package costofmachine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class countCost {
	
	public static void run(String from, String to) {
		try {
			Connection conn=DriverManager.getConnection("jdbc:mariadb://192.168.90.123/fatdb","listy","listy1234");
			PrintStream out;
			try {
				out = new PrintStream(new FileOutputStream("\\\\192.168.90.203\\Logistyka\\Raporty\\analiza maszyn\\analiza_maszyn_od_"+from+"_do_"+to+".txt"));
				System.setOut(out);
				System.setErr(out);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Nr maszyny;Typ;Nazwa;Klient;Cena;Waluta;Data;Material;Workprice;Montage;Construction;Electricians;CNC");
			//dla maszyn
			Statement c = conn.createStatement();
			ResultSet rs2 = c.executeQuery("Select nrMaszyny, opis, typ, klient, cena, waluta, DataKontrakt, komentarz from Calendar where Wyslano = 1 and nrMaszyny like '2/%' and klient is not null and klient <> 'MAGAZYN' and cena <> 0 and DataKontrakt >= '"+from+"' and DataKontrakt <= '"+to+"' order by typ asc, DataKontrakt asc" );
			while(rs2.next()) {
				String nrMaszyny = rs2.getString("nrMaszyny");
				String typ = rs2.getString("typ");
				String nazwa = rs2.getString("opis");
				String klient = rs2.getString("klient");
				String cena = rs2.getString("cena");
				String waluta = rs2.getString("waluta");
				String data = rs2.getString("DataKontrakt");
				String komentarz = rs2.getString("komentarz");
				
				//SQLs
				String sql_a = "select storenotesdetail.PROJECTNUMMER, NRMASZYNY, leverancier, storenotesdetail.artikelcode, artikelomschrijving, storenotesdetail.BESTELD, storenotesdetail.BESTELEENHEID, artikel_kostprijs.MATERIAAL as material, artikel_kostprijs.LONEN as workprice, artikel_kostprijs.cfstandaardeenheid from CALENDAR "
						+ "join storenotesdetail on storenotesdetail.PROJECTNUMMER like concat(calendar.nrmaszyny, '%') "
						+ "join artikel_kostprijs on storenotesdetail.artikelcode = artikel_kostprijs.ARTIKELCODE ";
				String sql_d = "select sum(bestellingdetail.suma), bestellingdetail.munt from bestelling " + 
						"join bestellingdetail on bestelling.leverancier = bestellingdetail.leverancier and bestelling.ORDERNUMMER = bestellingdetail.ORDERNUMMER ";
				String sql_b = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost NOT IN ('KM01', 'KE01', 'CNC') and (cfproject like '"+nrMaszyny+"%'";
				String sql_e = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost = 'KM01' and (cfproject like '"+nrMaszyny+"%'";
				String sql_f = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost = 'KE01' and (cfproject like '"+nrMaszyny+"%'";
				String sql_g = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost = 'CNC' and (cfproject like '"+nrMaszyny+"%'";
				
				if(komentarz.equals("")) {
					sql_a = sql_a + " where NrMaszyny = '"+nrMaszyny+"' order by projectnummer desc";
					sql_d = sql_d + " where concat(bestelling.AFDELING, \"/\", bestelling.AFDELINGSEQ) like '"+nrMaszyny+"%' " +
							"group by munt";
				}
				else {
					sql_a = sql_a + " where NrMaszyny = '"+nrMaszyny+"' or NrMaszyny like '"+komentarz+"' order by projectnummer desc";
					sql_d = sql_d + " where concat(bestelling.AFDELING, \"/\", bestelling.AFDELINGSEQ) like '"+nrMaszyny+"%' or concat(bestelling.AFDELING, \"/\", bestelling.AFDELINGSEQ) like '"+komentarz+"%' " +
							"group by munt";
					sql_b = sql_b + " or cfproject like '"+komentarz+"'";
					sql_e = sql_e + " or cfproject like '"+komentarz+"'";
					sql_f = sql_f + " or cfproject like '"+komentarz+"'";
					sql_g = sql_g + " or cfproject like '"+komentarz+"'";
				}
				sql_b = sql_b + ")";
				sql_e = sql_e + ")";
				sql_f = sql_f + ")";
				sql_g = sql_g + ")";
				
				double material = 0;
				double workprice = 0;
				double montage = 0;
				double constr = 0;
				double cnc = 0; 
				double el = 0;
				System.out.print(nrMaszyny+";"+typ+";"+nazwa+";"+klient+";"+cena+";"+waluta+";"+data+";");
				
				Statement a = conn.createStatement();
				ResultSet rs = a.executeQuery(sql_a);
				
				while(rs.next()){
					double ilosc = rs.getDouble("besteld");
					//check unit
					if(!rs.getString("besteleenheid").equals(rs.getString("cfstandaardeenheid"))) {
						Statement e = conn.createStatement();
						ResultSet rs4 = e.executeQuery("Select hoeveelheid from artikel_alteenh where artikelcode = '"+rs.getString("artikelcode")+"' and eenheid = '"+rs.getString("besteleenheid")+"'");
						while(rs4.next()) {
							ilosc = ilosc*rs4.getDouble("hoeveelheid");
						}
						rs4.close();
						e.close();
					}
					if(rs.getString("artikelcode").startsWith("%") ){
						double [] price = getPrice(rs.getString("artikelcode"));
						material+=ilosc*price[0];
						workprice+=ilosc*price[1];
						montage+=ilosc*price[2];
					}
					else{
						if(rs.getString("artikelcode").startsWith("KM")) 
							constr += ilosc*rs.getDouble("workprice");
						else 
							workprice+= ilosc*rs.getDouble("workprice");
						material+=ilosc*rs.getDouble("material");
						
					}
				}
				rs.close();
				a.close();
				
				//koszt montazu
				Statement b = conn.createStatement();
				ResultSet rs1 = b.executeQuery(sql_b);
				while(rs1.next()) {
					montage+=rs1.getDouble(1);
				}
				rs1.close();
				b.close();
				
				//koszt konstrukcji
				Statement e = conn.createStatement();
				ResultSet rs5 = e.executeQuery(sql_e);
				while(rs5.next()) {
					constr+=rs5.getDouble(1);
				}
				rs5.close();
				e.close();
				
				//koszt elektrykow
				Statement f = conn.createStatement();
				ResultSet rs6 = f.executeQuery(sql_f);
				while(rs6.next()) {
					el+=rs6.getDouble(1);
				}
				rs6.close();
				f.close();
				
				//koszt cnc
				Statement g = conn.createStatement();
				ResultSet rs7 = g.executeQuery(sql_g);
				while(rs7.next()) {
					cnc+=rs7.getDouble(1);
				}
				rs7.close();
				g.close();
				
				//koszt zamowien dodatkowych
				Statement d = conn.createStatement();
				
				ResultSet rs3 = d.executeQuery(sql_d);
				while(rs3.next()) {
					if(rs3.getString("munt").equals("EUR")) {
						material += rs3.getDouble(1)/4.1;
					}
					else material += rs3.getDouble(1);
				}
				
				/*System.out.println("Cena materialu "+material);
				System.out.println("Cena produkcji "+workprice);
				System.out.println("Cena montazu "+montage);*/
				System.out.println(String.format( "%.2f",material)+";"+String.format( "%.2f",workprice)+";"+String.format( "%.2f",montage)+";"+String.format( "%.2f",constr)+";"+String.format( "%.2f",el)+";"+String.format( "%.2f",cnc));
			}
			
			//dla innych projektow
			String sql_00 = "Select bestelling.leverancier, leverancier.naam from bestelling "
					+ "join leverancier on bestelling.leverancier = leverancier.leveranciernr " + 
					"where bestelling.leverdatum >= '"+from+"' and bestelling.leverdatum <= '"+to+"' and bestelling.leverancier < 50 and bestelling.leverancier not in (2, 6) " + 
					"group by leverancier";
			String sql_01 = "Select bestelling.leverancier, bestelling.ordernummer, storenotesdetail.artikelcode, storenotesdetail.besteld, storenotesdetail.besteleenheid, artikel_kostprijs.MATERIAAL as material, artikel_kostprijs.LONEN as workprice, artikel_kostprijs.cfstandaardeenheid from bestelling " + 
					"join storenotesdetail on bestelling.leverancier = storenotesdetail.afdeling and bestelling.ordernummer = storenotesdetail.afdelingseq " + 
					"join artikel_kostprijs on storenotesdetail.artikelcode = artikel_kostprijs.artikelcode " + 
					"where bestelling.leverdatum >= '"+from+"' and bestelling.leverdatum <= '"+to+"' and bestelling.leverancier = ";
			Statement st00 = conn.createStatement();
			ResultSet rs00 = st00.executeQuery(sql_00);
			while(rs00.next()) {
				String nrGrupy = rs00.getString(1);
				String nazwa = rs00.getString(1);
				double material = 0;
				double workprice = 0;
				double montage = 0;
				double constr = 0;
				double cnc = 0; 
				double el = 0;
				System.out.print(nrGrupy+";;"+nazwa+";;;;;");
				//pobierz cene czesci
				Statement st01 = conn.createStatement();
				ResultSet rs01 = st01.executeQuery(sql_01+rs00.getInt(1));
				while(rs01.next()) {
					double ilosc = rs01.getDouble("besteld");
					//check unit
					if(!rs01.getString("besteleenheid").equals(rs01.getString("cfstandaardeenheid"))) {
						Statement e = conn.createStatement();
						ResultSet rs4 = e.executeQuery("Select hoeveelheid from artikel_alteenh where artikelcode = '"+rs01.getString("artikelcode")+"' and eenheid = '"+rs01.getString("besteleenheid")+"'");
						while(rs4.next()) {
							ilosc = ilosc*rs4.getDouble("hoeveelheid");
						}
						rs4.close();
						e.close();
					}
					if(rs01.getString("artikelcode").startsWith("%") ){
						double [] price = getPrice(rs01.getString("artikelcode"));
						material+=ilosc*price[0];
						workprice+=ilosc*price[1];
						montage+=ilosc*price[2];
					}
					else{
						if(rs01.getString("artikelcode").startsWith("KM")) 
							constr += ilosc*rs01.getDouble("workprice");
						else 
							workprice+= ilosc*rs01.getDouble("workprice");
						material+=ilosc*rs01.getDouble("material");
						
					}
					
				}
				rs01.close();
				st01.close();
				
				String sql_d = "select sum(bestellingdetail.suma), bestellingdetail.munt from bestelling " + 
						"join bestellingdetail on bestelling.leverancier = bestellingdetail.leverancier and bestelling.ORDERNUMMER = bestellingdetail.ORDERNUMMER where bestelling.AFDELING= "+nrGrupy+" group by munt";
				String sql_b = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost NOT IN ('KM01', 'KE01', 'CNC') and (cfproject like '"+nrGrupy+"/%')";
				String sql_e = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost = 'KM01' and (cfproject like '"+nrGrupy+"/%')";
				String sql_f = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost = 'KE01' and (cfproject like '"+nrGrupy+"/%')";
				String sql_g = "select (100*sum(werktijdh)+floor(sum(werktijdm60)/60) + 10*mod(sum(werktijdm60), 60)/6) as montage from werkuren where werkpost = 'CNC' and (cfproject like '"+nrGrupy+"/%')";
				
				
				//koszt montazu
				Statement b = conn.createStatement();
				ResultSet rs1 = b.executeQuery(sql_b);
				while(rs1.next()) {
					montage+=rs1.getDouble(1);
				}
				rs1.close();
				b.close();
				
				//koszt konstrukcji
				Statement e = conn.createStatement();
				ResultSet rs5 = e.executeQuery(sql_e);
				while(rs5.next()) {
					constr+=rs5.getDouble(1);
				}
				rs5.close();
				e.close();
				
				//koszt elektrykow
				Statement f = conn.createStatement();
				ResultSet rs6 = f.executeQuery(sql_f);
				while(rs6.next()) {
					el+=rs6.getDouble(1);
				}
				rs6.close();
				f.close();
				
				//koszt cnc
				Statement g = conn.createStatement();
				ResultSet rs7 = g.executeQuery(sql_g);
				while(rs7.next()) {
					cnc+=rs7.getDouble(1);
				}
				rs7.close();
				g.close();
				
				//koszt zamowien dodatkowych
				Statement d = conn.createStatement();
				
				ResultSet rs3 = d.executeQuery(sql_d);
				while(rs3.next()) {
					if(rs3.getString("munt").equals("EUR")) {
						material += rs3.getDouble(1)/4.1;
					}
					else material += rs3.getDouble(1);
				}
				
				/*System.out.println("Cena materialu "+material);
				System.out.println("Cena produkcji "+workprice);
				System.out.println("Cena montazu "+montage);*/
				System.out.println(String.format( "%.2f",material)+";"+String.format( "%.2f",workprice)+";"+String.format( "%.2f",montage)+";"+String.format( "%.2f",constr)+";"+String.format( "%.2f",el)+";"+String.format( "%.2f",cnc));
			
			}
			rs00.close();
			st00.close();		
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static double[] getPrice(String articlecode) throws SQLException{
		Connection conn=DriverManager.getConnection("jdbc:mariadb://192.168.90.123/fatdb","listy","listy1234");
		double[] tab = new double[3];
		//koszt materialow
		tab[0] = 0; 
		//koszt produkcji 
		tab[1] = 0; 
		//koszt montazu
		tab[2] = 0;
				
		Statement a = conn.createStatement();
		ResultSet rs = a.executeQuery("Select lonen from artikel_kostprijs where artikelcode = '"+articlecode+"' and soort = 4");
		while(rs.next()){
			//System.out.println("Cena montazu "+articlecode+" "+rs.getDouble(1));
			tab[2]+=rs.getDouble(1);
		}
		a.close();
		rs.close();
		
		Statement b = conn.createStatement();
		ResultSet rs2 = b.executeQuery("select onderdeel, typ, ilosc, jednostka, materiaal as material, lonen as workprice, artikel_kostprijs.cfstandaardeenheid from struktury left join artikel_kostprijs on struktury.onderdeel = artikel_kostprijs.artikelcode where struktury.artikelcode = '"+articlecode+"' and artikel_kostprijs.soort = 4");
		while(rs2.next()){
			double ilosc = rs2.getDouble("ilosc");
			//check unit
			if(!rs2.getString("jednostka").equals(rs2.getString("cfstandaardeenheid"))) {
				Statement e = conn.createStatement();
				ResultSet rs4 = e.executeQuery("Select hoeveelheid from artikel_alteenh where artikelcode = '"+rs2.getString("onderdeel")+"' and eenheid = '"+rs2.getString("jednostka")+"'");
				while(rs4.next()) {
					ilosc = ilosc*rs4.getDouble("hoeveelheid");
				}
				rs4.close();
				e.close();
			}
			
			if(rs2.getString("typ").equals("F")||rs2.getString("onderdeel").startsWith("%")){
				double[] tab_tmp = getPrice(rs2.getString("onderdeel"));
				tab[0]+=ilosc*tab_tmp[0];
				tab[1]+=ilosc*tab_tmp[1];
				
			}
			else{
				tab[0] += ilosc*rs2.getDouble("material");
				tab[1] += ilosc*rs2.getDouble("workprice"); 
			}
		}
		b.close();
		rs2.close();
		conn.close();
		return tab;
		
	}
	

}
