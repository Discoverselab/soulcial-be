package org.springblade.modules.admin.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springblade.modules.admin.pojo.po.StatisticsDataPONew;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Auther: FengZi
 * @Date: 2024/1/12 14:41
 * @Description:
 */
public class StatisticsUtils {

	public static void getVerticalData(List<StatisticsDataPONew> data) {
		// 创建一个Workbook对象，对应一个Excel文件
		Workbook workbook = new XSSFWorkbook();

		// 在Workbook中添加一个sheet，对应Excel文件中的工作表
		Sheet sheet = workbook.createSheet("Data Transpose");

		Map<String, Map<String, Object>> verticalData = fetchData(data);

		// 填充Excel表头
		Row headerRow = sheet.createRow(0);
		int dateCellIndex = 1;
		for (String date : verticalData.get("a1").keySet()) {
			Cell cell = headerRow.createCell(dateCellIndex++);
			cell.setCellValue(date);
		}

		// 填充Excel数据行
		int rownum = 1;
		for (Map.Entry<String, Map<String, Object>> entry : verticalData.entrySet()) {
			Row row = sheet.createRow(rownum++);
			Cell typeCell = row.createCell(0);
			typeCell.setCellValue(entry.getKey());

			int cellIndex = 1;
			for (Object value : entry.getValue().values()) {
				Cell cell = row.createCell(cellIndex++);
				if (value instanceof Number) {
					cell.setCellValue(((Number) value).doubleValue());
				} else {
					if (value == null){
						value = "0";
					}
					cell.setCellValue(value.toString());
				}
			}
		}

		// 自动调整所有单元格宽度
		for (int i = 0; i < dateCellIndex; i++) {
			sheet.autoSizeColumn(i);
		}

		// 将文件保存到指定的位置
		try (FileOutputStream outputStream = new FileOutputStream("DataTranspose.xlsx")) {
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Excel file written successfully...");
	}


	private static Map<String, Map<String, Object>> fetchData(List<StatisticsDataPONew> data) {
		// 创建一个列表，用于存储模拟的数据库结果集。
		List<Map<String, Object>> horizontalData = new ArrayList<>();

		// 初始化数据
		for (StatisticsDataPONew datum : data) {
			horizontalData.add(createMap(
				"date", datum.getDate(),
				"a1", datum.getA1(),
				"a2", datum.getA2(),
				"a3", datum.getA3(),
				"a4", datum.getA4(),
				"a5", datum.getA5(),
				"a6", datum.getA6(),
				"a7", datum.getA7(),
				"a8", datum.getA8(),
				"a9", datum.getA9(),
				"a10", datum.getA10(),
				"a11", datum.getA11(),
				"a12", datum.getA12(),
				"a13", datum.getA13(),
				"a14", datum.getA14(),
				"a15", datum.getA15(),
				"a16", datum.getA16(),
				"a17", datum.getA17(),
				"a18", datum.getA18(),
				"a19", datum.getA19(),
				"a20", datum.getA20()
				));
		}

		// 使用LinkedHashMap来保持插入顺序
		Map<String, Map<String, Object>> verticalData = new LinkedHashMap<>();

		// 初始化竖表的数据结构。
		verticalData.put("a1", new LinkedHashMap<>());
		verticalData.put("a2", new LinkedHashMap<>());
		verticalData.put("a3", new LinkedHashMap<>());
		verticalData.put("a4", new LinkedHashMap<>());
		verticalData.put("a5", new LinkedHashMap<>());
		verticalData.put("a6", new LinkedHashMap<>());
		verticalData.put("a7", new LinkedHashMap<>());
		verticalData.put("a8", new LinkedHashMap<>());
		verticalData.put("a9", new LinkedHashMap<>());
		verticalData.put("a10", new LinkedHashMap<>());
		verticalData.put("a11", new LinkedHashMap<>());
		verticalData.put("a12", new LinkedHashMap<>());
		verticalData.put("a13", new LinkedHashMap<>());
		verticalData.put("a14", new LinkedHashMap<>());
		verticalData.put("a15", new LinkedHashMap<>());
		verticalData.put("a16", new LinkedHashMap<>());
		verticalData.put("a17", new LinkedHashMap<>());
		verticalData.put("a18", new LinkedHashMap<>());
		verticalData.put("a19", new LinkedHashMap<>());
		verticalData.put("a20", new LinkedHashMap<>());

		// 遍历横表数据，填充竖表结构。
		for (Map<String, Object> row : horizontalData) {
			String date = (String) row.get("date");
			String a1 = (String) row.get("a1");
			String a2 = (String) row.get("a2");
			String a3 = (String) row.get("a3");
			String a4 = (String) row.get("a4");
			String a5 = (String) row.get("a5");
			String a6 = (String) row.get("a6");
			String a7 = (String) row.get("a7");
			String a8 = (String) row.get("a8");
			String a9 = (String) row.get("a9");
			String a10 = (String) row.get("a10");
			String a11 = (String) row.get("a11");
			String a12 = (String) row.get("a12");
			String a13 = (String) row.get("a13");
			String a14 = (String) row.get("a14");
			String a15 = (String) row.get("a15");
			String a16 = (String) row.get("a16");
			String a17 = (String) row.get("a17");
			String a18 = (String) row.get("a18");
			String a19 = (String) row.get("a19");
			String a20 = (String) row.get("a20");

			verticalData.get("a1").put(date, a1);
			verticalData.get("a2").put(date, a2);
			verticalData.get("a3").put(date, a3);
			verticalData.get("a4").put(date, a4);
			verticalData.get("a5").put(date, a5);
			verticalData.get("a6").put(date, a6);
			verticalData.get("a7").put(date, a7);
			verticalData.get("a8").put(date, a8);
			verticalData.get("a9").put(date, a9);
			verticalData.get("a10").put(date, a10);
			verticalData.get("a11").put(date, a11);
			verticalData.get("a12").put(date, a12);
			verticalData.get("a13").put(date, a13);
			verticalData.get("a14").put(date, a14);
			verticalData.get("a15").put(date, a15);
			verticalData.get("a16").put(date, a16);
			verticalData.get("a17").put(date, a17);
			verticalData.get("a18").put(date, a18);
			verticalData.get("a19").put(date, a19);
			verticalData.get("a20").put(date, a20);
		}
		return verticalData;
	}

	// 一个辅助方法，用于在JDK 8中构建Map实例
	private static Map<String, Object> createMap(Object... values) {
		if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }

		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < values.length; i += 2) {
			map.put((String) values[i], values[i + 1]);
		}
		return map;
	}


	// 获取从指定日期到今天日期的所有日期的字符串列表
	public static List<String> getDateList(String startDateStr) {
		LocalDate startDate = LocalDate.parse(startDateStr);
		LocalDate endDate = LocalDate.now(); // 使用当前日期作为结束日期

		List<String> dates = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		// 循环从开始日期到结束日期，将每个日期转换为字符串并添加到列表中
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			dates.add(date.format(formatter));
		}

		return dates;
	}
}
