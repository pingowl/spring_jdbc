package gov.example.appDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Vector;


@SpringBootApplication
@Component
public class AppDemoApplication implements CommandLineRunner {

	public static class SameNameInsertError extends RuntimeException {

	}
	public static class SameNameSearchedError extends RuntimeException {

	}
	public static class NoResultError extends RuntimeException {

	}
	public static class SameEmailError extends RuntimeException {

	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {

		SpringApplication.run(AppDemoApplication.class, args);


	}
	public int SameNameCount(String name){
		String sql = String.format("SELECT COUNT(*) FROM students WHERE students.name = '%s';", name);
		int res=0;
		try{
			int val = jdbcTemplate.queryForObject(sql, Integer.class);
			res += val;
		} catch (IncorrectResultSizeDataAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.printf("\n================= SAME NAME COUNT : %d\n", res);
		return res;
	}

	public void Insert(String name, String email, String graduation, String degree) {
		// sid, name, email, degree
		String sql = String.format("INSERT INTO students (name, email, graduation, degree) VALUES ('%s','%s','%s','%s')"
				,name,email,graduation,degree);
		int rows=0;
		try{
			rows = jdbcTemplate.update(sql);
		} catch (DuplicateKeyException e) {
			throw new SameEmailError();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (rows > 0) {
			System.out.printf(">> One row inserted.\n");
		}
	}

	public String ReturnDegree(String name) {
		// sid, name, email, degree
		String sql = String.format("SELECT degree FROM students WHERE students.name = '%s';"
				,name);
		String a = "";
		try{

			 if (SameNameCount(name)>1){
				 throw new SameNameSearchedError();
			 }
			String res = jdbcTemplate.queryForObject(sql, String.class);
			 a += res;

		} catch (IncorrectResultSizeDataAccessException e) {
			throw new NoResultError();
		} catch (SameNameSearchedError e) {
			throw new SameNameSearchedError();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	public String ReturnEmail(String name)  {
		// sid, name, email, degree
		String sql = String.format("SELECT email FROM students WHERE students.name = '%s';"
				,name);
		String a = "";
		try{

			if (SameNameCount(name)>1){
				throw new SameNameSearchedError();
			}
			String res = jdbcTemplate.queryForObject(sql, String.class);
			a += res;
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new NoResultError();
		} catch (SameNameSearchedError e) {
			throw new SameNameSearchedError();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	public int ReturnCount(String degree) {
		// sid, name, email, degree
		String sql = String.format("SELECT COUNT(*) FROM students WHERE students.degree= '%s';"
				,degree);
		int res=0;
		try{
			int val = jdbcTemplate.queryForObject(sql, Integer.class);
			res +=val;
		} catch (IncorrectResultSizeDataAccessException e) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	String[] degreeList = new String[3];
	Vector<String> studentGroup1 = new Vector<>();
	Vector<String> studentGroup2 = new Vector<>();
	Vector<String> studentGroup3 = new Vector<>();
	Vector<Vector<String>> v = new Vector<>();
	@Override
	public void  run(String... args) throws Exception   {
		// scrap student info and insert data into database
		Document doc = null;
		try{
			String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";
			doc = Jsoup.connect("https://apl.hongik.ac.kr/lecture/dbms")
					.header("Content-Type", "application/json;charset=UTF-8")
					.userAgent(USER_AGENT)
					.get();

			
		
		} catch (IOException e) {
			e.printStackTrace();
		}

		Elements degree1Elements = doc.select( "h2[id=h.cwxa41cyxn28_l]");
		Elements degree2Elements = doc.select("h2[id=h.xrleu3h82rn1_l]");
		Elements degree3Elements = doc.select("h2[id=h.kfl1x21a81ct_l]");
		Elements deg1StuElements = doc.select( "h2[id=h.cwxa41cyxn28_l] + ul > li");
		Elements deg2StuElements = doc.select("h2[id=h.xrleu3h82rn1_l] + ul > li");
		Elements deg3StuElements = doc.select("h2[id=h.kfl1x21a81ct_l] + ul > li");


		degreeList[0]=degree1Elements.text().strip();
		degreeList[1]=degree2Elements.text().strip();
		degreeList[2]=degree3Elements.text().strip();

		for(Element  element : deg1StuElements){
			studentGroup1.add(element.text().strip());
		}
		v.add(studentGroup1);
		for(Element  element : deg2StuElements){
			studentGroup2.add(element.text().strip());
		}
		v.add(studentGroup2);
		for(Element  element : deg3StuElements){
			studentGroup3.add(element.text().strip());
		}
		v.add(studentGroup3);


		// degree 일치 확인
		String[] degarr = new String[3];
		for(int i=0; i<3; i++){
			if(degreeList[i].equals("PhD Students")){
				degarr[i] ="phd";
			}
			else if (degreeList[i].equals("Master Students")){
				degarr[i] ="master";
			}
			else if (degreeList[i].equals("Undergraduate Students")){
				degarr[i] ="undergrad";
			}
		}


		// postgres 에 스크래핑 정보 저장
		// 이름, 이메일, 졸업년도 파싱
		for(int i=0; i<3; i++){
			String degree = degarr[i];
			for(String line : v.get(i)){
				String[] strArr = line.split(", ");
				
				try {
					Insert(strArr[0], strArr[1], strArr[2], degree); // name, email, graduation, degree
				} catch (SameEmailError e) {
					System.out.println("\n=== DuplicateKeyException Occured! ===\n");
				}
			}
		}

		// for test
//		System.out.println("\n=======================");
//
//		for(String degree : degarr){
//			System.out.println(degree);
//		}
//		for(int i=0; i<3; i++){
//			for(String s : v.get(i)){
//				System.out.println(s);
//			}
//			System.out.println("---");
//		}
//
//		System.out.println("=======================\n");

		//return textDeg1;
	}




}
