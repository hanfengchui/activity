package com.timesontransfar.common.analyzer.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.*;

import org.apache.commons.lang.StringUtils;

import com.timesontransfar.common.analyzer.ISqlAnalyzer;
import com.timesontransfar.common.util.LocalUtil;
import com.transfar.common.exception.MyOwnRuntimeException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SqlAnalyzerImpl implements ISqlAnalyzer {
	
	public static final String WHERE = " WHERE ";
	public static final String GROUP_BY = " GROUP BY ";
	public static final String FROM = " FROM ";
	public static final String WEB = "WEB__";
	public static final String MESSAGE = "获取查询参数发生异常,异常信息是:";

	/**
	 * 
	 */
	public String[] getParameter(String sql) throws RuntimeException {
		try {
			List conditionList = new ArrayList();
			String newSql = "";
			newSql = this.readSql(sql);
			int whereIndex = newSql.indexOf(WHERE);
			if (whereIndex < 0) {
				return new String[0];
			}
			String whereClause = newSql.substring(whereIndex + 7).trim();
			int groupIndex = whereClause.indexOf(GROUP_BY);
			if (groupIndex > 0) {
				whereClause = whereClause.substring(0, groupIndex);
			}
			
			Map aliasMap = this.getAliasMap(newSql);
			List whereArray = new ArrayList();
			int quoteIndex = whereClause.indexOf("?");
			while (quoteIndex >= 0) {
				whereArray.add(whereClause.substring(0, quoteIndex).trim());
				whereClause = whereClause.substring(quoteIndex + 1).trim();
				quoteIndex = whereClause.indexOf("?");
			}
			for (int i = 0; i < whereArray.size(); i++) {
				String[] subClauseArray = ((String) whereArray.get(i)).trim()
						.split(" ");
				StringReader conditionReader = null;
				if (subClauseArray[subClauseArray.length - 1].trim()
						.equals("%")) {
					conditionReader = new StringReader(
							subClauseArray[subClauseArray.length - 3].trim());
				} else if (subClauseArray[subClauseArray.length - 1].trim()
						.equals("LIKE")) {
					conditionReader = new StringReader(
							subClauseArray[subClauseArray.length - 2].trim());
				} else {
					conditionReader = new StringReader(
							subClauseArray[subClauseArray.length - 1].trim());
				}
				StringBuffer conditionBuffer = new StringBuffer();
				int readChar = conditionReader.read();
				while (readChar > 0) {
					if (((readChar >= 65) && (readChar <= 90))
							|| (readChar == 46) || (readChar == 95)) {
						char x = (char) readChar;
						conditionBuffer.append(x);
					}
					readChar = conditionReader.read();
				}
				String condition = conditionBuffer.toString();
				String parameter = "";
				int stopIndex = condition.indexOf(".");
				if (stopIndex >= 0) {
					String table = (String) aliasMap.get(condition.substring(0,
							stopIndex));
					String column = condition.substring(stopIndex + 1);
					parameter = table + "__" + column;
				} else {
					parameter = condition;
				}
				conditionList.add(parameter);
			}
			String[] returnParameter = new String[conditionList.size()];
			for (int i = 0; i < returnParameter.length; i++) {
				returnParameter[i] = (String) conditionList.get(i);
			}
			conditionList.clear();
			conditionList = null;
			return returnParameter;
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String message = new String(writer.getBuffer());
			throw new MyOwnRuntimeException(MESSAGE + message);
		}
	}

	public Map getAliasMap(String sql) {
		Map aliasMap = new TreeMap();
		int fromIndex = sql.indexOf(FROM);
		int whereIndex = sql.indexOf(WHERE);
		if (whereIndex < 0) {
			whereIndex = sql.length();
		}
		if (fromIndex < 0) {
			return aliasMap;
		}
		String tableSql = sql.substring(fromIndex + 6, whereIndex).trim();
		String[] alias = tableSql.split(",");
		for (int i = 0; i < alias.length; i++) {
			String[] tableAlias = alias[i].trim().split(" ");
			if (tableAlias.length > 1) {
				aliasMap.put(tableAlias[1], tableAlias[0]);
			} else {
				aliasMap.put(tableAlias[0], tableAlias[0]);
			}
		}
		return aliasMap;
	}

	public Map getTableMap(String sql) {
		Map tableMap = new TreeMap();
		int fromIndex = sql.indexOf(FROM);
		int whereIndex = sql.indexOf(WHERE);
		if (whereIndex < 0) {
			whereIndex = sql.length();
		}
		if (fromIndex < 0) {
			return tableMap;
		}
		String tableSql = sql.substring(fromIndex + 6, whereIndex).trim();
		String[] alias = tableSql.split(",");
		for (int i = 0; i < alias.length; i++) {
			String[] tableAlias = alias[i].trim().split(" ");
			if (tableAlias.length > 1) {
				tableMap.put(tableAlias[0], tableAlias[1]);
			} else {
				tableMap.put(tableAlias[0], tableAlias[0]);
			}
		}
		return tableMap;
	}

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public Map getWebId(String newSql) throws RuntimeException {
		Map aliasMap = this.getAliasMap(newSql);
		Map webIdMap = new TreeMap();
		int selectIndex = newSql.indexOf("SELECT ");
		int fromIndex = newSql.indexOf(FROM);
		if ((selectIndex >= 0) && (fromIndex >= 0)) {
			String columnClause = newSql.substring(selectIndex + 7, fromIndex)
					.trim();
			String[] column = columnClause.split(",");
			for (int i = 0; i < column.length; i++) {
				int asIndex = column[i].indexOf(" AS ");
				if (asIndex >= 0) {
					String label = column[i].substring(0, asIndex).trim();
					String alias = column[i].substring(asIndex + 4).trim();
					int dotIndex = label.indexOf(".");
					String webId = null;
					if (dotIndex >= 0) {
						String displayTable = label.substring(0, dotIndex);
						String realTable = ((String) aliasMap.get(displayTable))
								.trim();
						String realColumn = label.substring(dotIndex + 1)
								.trim();
						webId = realTable + "__" + realColumn + "__" + alias
								+ "__";
					}
					webIdMap.put(alias, webId);
				} else {
					String label = column[i].trim();
					String alias = "";
					int dotIndex = label.indexOf(".");
					String webId = null;
					if (dotIndex >= 0) {
						String displayTable = label.substring(0, dotIndex);
						String realTable = ((String) aliasMap.get(displayTable))
								.trim();
						String realColumn = label.substring(dotIndex + 1)
								.trim();
						alias = realColumn;
						webId = realTable + "__" + realColumn + "__" + alias
								+ "__";
					}
					webIdMap.put(alias, webId);
				}
			}
		}
		return webIdMap;
	}

	public Map getColumnAliasMap(String newSql) throws RuntimeException {
		Map webIdMap = new TreeMap();
		int selectIndex = newSql.indexOf("SELECT ");
		int fromIndex = newSql.indexOf("FROM ");
		if ((selectIndex >= 0) && (fromIndex >= 0)) {
			String columnClause = newSql.substring(selectIndex + 7, fromIndex)
					.trim();
			String[] column = columnClause.split(",");
			for (int i = 0; i < column.length; i++) {
				int asIndex = column[i].indexOf(" AS ");
				if (asIndex >= 0) {
					String label = column[i].substring(0, asIndex).trim();
					String alias = column[i].substring(asIndex + 4).trim();
					int dotIndex = label.indexOf(".");
					String displayTable = null;
					if (dotIndex >= 0) {
						displayTable = label.substring(0, dotIndex);
					}
					webIdMap.put(alias, displayTable);
				} else {
					String label = column[i].trim();
					String alias = " ";
					int dotIndex = label.indexOf(".");
					String displayTable = null;
					if (dotIndex >= 0) {
						String realColumn = label.substring(dotIndex + 1)
								.trim();
						alias = realColumn;
						displayTable = label.substring(0, dotIndex);
					}
					webIdMap.put(alias, displayTable);
				}
			}
		}
		return webIdMap;
	}
	
	/**
	 * 添加By qliang，原有方法只简单的转换大小写，而在实际应用中不需要将查询字段中的查询内容转换成大写
	 * @param sql
	 * @return
	 */
	private String convert2UpSql(String sql){
		StringBuilder sqlStr = new StringBuilder("");
		int count = StringUtils.countMatches(sql,"'");
		if (count <= 0 || count%2!=0) {
			return sql.toUpperCase();
		}
		//根据需要，替换相应的字符串
		String dotSql = sql;
		for (int i=0;i<count/2;i++){
		   int dotIndex=dotSql.indexOf("'");
		   sqlStr.append(dotSql.substring(0,dotIndex+1).toUpperCase());
		   dotSql = dotSql.substring(dotIndex+1);//对此SQL进行分析
		   dotIndex=dotSql.indexOf("'");
		   sqlStr.append(dotSql.substring(0,dotIndex+1));
		   dotSql = dotSql.substring(dotIndex+1);
		   if (i==(count/2-1)){
			   sqlStr.append(dotSql.toUpperCase());
		   }
		}
		return sqlStr.toString();
	}

	public String readSql(String sql) throws RuntimeException {
		try {
			String upperSql = this.convert2UpSql(sql);
			StringReader reader = new StringReader(upperSql);
			StringBuffer newSql = new StringBuffer();
			int oldChar;
			int readChar = reader.read();
			if ((readChar == '\n') || (readChar == '\t')) {
				readChar = 32;
			}
			int savedChar = 0;
			char x;
			while (readChar > 0) {
				oldChar = readChar;
				if (oldChar > 32) {
					x = (char) oldChar;
					newSql.append(x);
					savedChar = x;
					readChar = reader.read();
					if ((readChar == '\n') || (readChar == '\t')) {
						readChar = 32;
					}
				} else {
					readChar = reader.read();
					if ((readChar == '\n') || (readChar == '\t')) {
						readChar = 32;
					}
					// 字符集范围,如果前面的字符为空格,当前字符在A-Z范围内,0-9范围内,'(','%',*,以及当前保存的字符不等于'
					if ((((readChar >= 65) && (readChar <= 90))
							|| (readChar == 40) || (readChar == 37)
							|| ((readChar >= 48) && (readChar <= 57)) || (readChar == 42))
							&& (oldChar == 32) && (savedChar != 44)) {
						/*
						 * readChar in range of A TO Z,or readchar is in
						 * {'*','(','%'},and oldChar is not space and savedChar
						 * is not ','.
						 */
						x = (char) oldChar;
						newSql.append(x);
						savedChar = x;
					}
					if ((savedChar == 69) && (oldChar == 32)
							&& (readChar == 63)) {
						// Estimate the case of "LIKE ?%"
						x = (char) oldChar;
						newSql.append(x);
						savedChar = x;
					}
				}
			}
			return newSql.toString();
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String message = new String(writer.getBuffer());
			throw new MyOwnRuntimeException(MESSAGE + message);
		}
	}

	public String addCondition(String sql, String webCondition) {
		String newSql = this.readSql(sql);
		Map tableMap = this.getTableMap(newSql);
		return this.addCondition(tableMap, newSql, webCondition);
	}

	/**
	 * 得到某字符串以后的所有的字符串
	 * 
	 * @param allString
	 *            得到所有的字符串
	 * @param partStr
	 *            需要分割的字符
	 * @return add by qliang 2006-08-03
	 */
	public String getLastStringPartString(String allString, String partStr) {
		String result = allString;
		int lastIndex = result.lastIndexOf(partStr);
		if (lastIndex > 0) {
			return result.substring(lastIndex, result.length());
		} else {
			return result;
		}
	}

	/**
	 * 根据表名Map,添加自定义条件
	 * 
	 * @param tableMap
	 * @param webCondition
	 * @return
	 */
	public String addCondition(Map tableMap, String newSql, String webCondition)
			throws RuntimeException {
		try {
			boolean findFlag = false;
			String temp = webCondition.toUpperCase().trim();
			String originalCondition = webCondition;
			String condition = " ";
			String groupSql = "";
			// modify by qliang 2006-08-03
			// 查找到最后一个SQL语句最后的FROM字符串
			String fromEndSql = getLastStringPartString(newSql, FROM);
			int whereIndex = fromEndSql.indexOf(WHERE);
			if (whereIndex > 0) {
				condition = " AND (";
			} else {
				condition = " WHERE (";
			}
			//wanrongwei 最后的FROM 中加入 ROWNUM
			
			int groupIndex = newSql.indexOf(GROUP_BY);
			if (groupIndex > 0) {
				findFlag = true;
				groupSql = newSql.substring(groupIndex);
				newSql = newSql.substring(0, groupIndex);
			}
			if (!findFlag) {
				groupIndex = newSql.indexOf(" ORDER BY ");
				if (groupIndex > 0) {
					findFlag = true;
					groupSql = newSql.substring(groupIndex);
					newSql = newSql.substring(0, groupIndex);
				}
			}
			
			String entireDatePattern = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\ [0-9]{2}:[0-9]{2}:[0-9]{2}";
			String entireDatePattern1 = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\ [0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\.[0-9]{1,3}";
			String shortDatePattern = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}";
			if (tableMap != null) {
				int labelIndex = temp.indexOf(WEB);
				while (labelIndex >= 0) {
					String value = originalCondition.substring(0, labelIndex);
					if ((Pattern.matches(entireDatePattern, value))
							|| (Pattern.matches(entireDatePattern1, value))
							|| (Pattern.matches(shortDatePattern, value))) {
						if (value.length() > 19) {
							value = value.substring(0, 19);
						}
						value = " to_date('" + value
								+ "','yyyy-mm-dd HH24:MI:SS')";
					}
					condition += value;
					temp = temp.substring(labelIndex + 5);
					originalCondition = originalCondition
							.substring(labelIndex + 5);
					StringReader reader = new StringReader(temp);
					StringBuffer buffer = new StringBuffer();
					int readChar = reader.read();
					int readCount = 1;
					while (readChar > 0) {
						if (((readChar >= 65) && (readChar <= 90))
								|| (readChar == 95)) {
							char x = (char) readChar;
							buffer.append(x);
						} else if ((readChar == 32)
								|| ((readChar >= 60) && (readChar <= 62))) {
							break;
						}
						readChar = reader.read();
						readCount++;
					}
					String tableColumn = buffer.toString().trim();
					int underLineIndex = tableColumn.indexOf("__");
					if (underLineIndex >= 0) {
						String table = tableColumn.substring(0, underLineIndex);
						String column = tableColumn
								.substring(underLineIndex + 2);
						// 罗翔于20060610修改，丢弃WEBID后面的与Table 和 Column无关的东东
						int tempIndex = column.indexOf("__");
						if (tempIndex > 0) {
							column = column.substring(0, tempIndex);
						}
						table = (String) tableMap.get(table);
						tableColumn = table + "." + column;
						condition += tableColumn;
					}
					temp = temp.substring(readCount - 1);
					originalCondition = originalCondition
							.substring(readCount - 1);
					labelIndex = temp.indexOf(WEB);
				}
				if (labelIndex < 1) {
					condition += originalCondition;
				}
				newSql += condition + ") ";
				newSql += groupSql;
				return newSql;
			} else {
				return newSql;
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String message = new String(writer.getBuffer());
			throw new MyOwnRuntimeException(MESSAGE + message);
		}
	}

	/**
	 * 
	 */
	public String addCondition(String sql, Map inParam) {
		String newSql = this.readSql(sql);
		Map tableMap = this.getTableMap(newSql);
		return this.addCondition(tableMap, this.getColumnAliasMap(sql), newSql,
				inParam);
	}

	/**
	 * 自适应参数添加条件
	 * 修改BY Qliang，添加可以判断Order By条件的权限因子
	 * @param tableMap
	 * @param Sql
	 * @param inParam
	 * @return
	 * @throws IOException
	 */
	public String addCondition(Map tableMap, Map columnAliasMap, String newSql,
			Map inParam) throws RuntimeException {
		String condition = null;
		boolean findFlag = false;
		int whereIndex = newSql.indexOf(WHERE);
		if (whereIndex > 0) {
			condition = " AND (";
		} else {
			condition = " WHERE (";
		}
		String groupSql = "";
		int groupIndex = newSql.indexOf(GROUP_BY);
		if (groupIndex > 0) {
			findFlag = true;
			groupSql = newSql.substring(groupIndex);
			newSql = newSql.substring(0, groupIndex);
		}
		if (!findFlag) {
			groupIndex = newSql.indexOf(" ORDER BY ");
			if (groupIndex > 0) {
				findFlag = true;
				groupSql = newSql.substring(groupIndex);
				newSql = newSql.substring(0, groupIndex);
			}
		}
		// 时间模版，符合下面三个模版的将会被认为是日期型参数
		String entireDatePattern = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\ [0-9]{2}\\:[0-9]{2}\\:[0-9]{2}";
		String entireDatePattern1 = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\ [0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\.[0-9]{1,3}";
		String shortDatePattern = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}";
		if (tableMap != null) {
			Iterator iterator = inParam.keySet().iterator();
			boolean first = true;
			while (iterator.hasNext()) {
				String webId = (String) iterator.next();
				String value = (String) inParam.get(webId);
				boolean hasValue = false;
				if (value != null && value.trim().length() > 0) {
						hasValue = true;
						value = LocalUtil.decode(value);
				}
				if (hasValue) {
					String tableColumn = webId.toUpperCase();
					int uIndex = tableColumn.indexOf(WEB);
					if (uIndex >= 0) {
						tableColumn = tableColumn.substring(uIndex + 5);
						int underLineIndex = tableColumn.indexOf("__");
						if (underLineIndex >= 0) {
							String table = tableColumn.substring(0,
									underLineIndex).trim();
							if (tableMap.containsKey(table)) {
								String column = tableColumn
										.substring(underLineIndex + 2);
								int tempIndex = column.indexOf("__");
								if (tempIndex > 0) {
									String alias = column
											.substring(tempIndex + 2);// 取别名
									column = column.substring(0, tempIndex);
									if (alias != null) {
										tempIndex = alias.indexOf("__");
										if (tempIndex > 0) {
											alias = alias.substring(0,
													tempIndex);
										}
									}
									if (alias != null) {// 如果别名不为空，则从列的别名Map中取表的别名
										table = (String) columnAliasMap
												.get(alias) == null ? (String) tableMap
												.get(table)
												: (String) columnAliasMap
														.get(alias);
									} else {
										table = (String) tableMap.get(table);
									}
									tableColumn = table + "." + column;
									if (!first) {
										condition += " AND ";
									} else {
										first = false;
									}
									int inIndex = value.indexOf("||");// 判断传入的参数是否存在多个，是否需要使用In子句
									if (inIndex > 0) {
										condition += tableColumn + " IN (";
										value += "||";
										while (inIndex > 0) {
											condition += "'"
													+ value.substring(0,
															inIndex) + "',";
											if (value.length() > inIndex + 2) {
												value = value
														.substring(inIndex + 2);
												inIndex = value.indexOf("||");
											} else {
												inIndex = 0;
											}
										}
										condition = condition.substring(0,
												condition.length() - 1)
												+ ")";
									} else {
										condition += tableColumn + " = ";
										if ((Pattern.matches(entireDatePattern,
												value))
												|| (Pattern.matches(
														entireDatePattern1,
														value))
												|| (Pattern
														.matches(
																shortDatePattern,
																value))) {
											if (value.length() > 19) {
												value = value.substring(0, 19);
											}
											// 如果是日期型参数，将进行转换
											value = " to_date('"
													+ value
													+ "','yyyy-mm-dd HH24:MI:SS')";
											condition += value;
										} else {
											condition += "'" + value + "'";
										}
									}
								}
							}
						}
					}
				}
			}
			if (condition.length() > 10) {
				newSql += condition + ")";
				return newSql + groupSql;
			}
		}
		return newSql + groupSql;
	}

	public String generateCountSql(String sql) {
		return "SELECT COUNT(*) FROM (" + sql + ") AS COUNT";
	}

	public String generateBetweenSql(String sql, int begin, int limit) {
		return sql + " LIMIT " + begin + ", " + limit;
	}

}
