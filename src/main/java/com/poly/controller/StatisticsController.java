package com.poly.controller;

import com.poly.entity.Account;
import com.poly.entity.Category;
import com.poly.entity.Order;
import com.poly.entity.OrderDetail;
import com.poly.service.AccountService;
import com.poly.service.CategoryService;
import com.poly.service.OrderDetailService;
import com.poly.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class StatisticsController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private AccountService accountService;

	@RequestMapping("/admin/statistics")
	public String statistics(Model model) {
		// Thống kê theo loại hàng
		List<Category> categories = categoryService.findAll();
		List<CategoryStatistics> categoryStats = categories.stream().map(cat -> {
			List<OrderDetail> details = orderDetailService.findAll().stream()
					.filter(od -> od.getProduct() != null && od.getProduct().getCategory().getId().equals(cat.getId()))
					.collect(Collectors.toList());
			BigDecimal totalRevenue = details.stream()
					.map(od -> od.getPrice().multiply(new BigDecimal(od.getQuantity())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			int totalQuantity = details.stream().mapToInt(OrderDetail::getQuantity).sum();
			BigDecimal maxPrice = details.stream().map(OrderDetail::getPrice).max(BigDecimal::compareTo)
					.orElse(BigDecimal.ZERO);
			BigDecimal minPrice = details.stream().map(OrderDetail::getPrice).min(BigDecimal::compareTo)
					.orElse(BigDecimal.ZERO);
			BigDecimal avgPrice = BigDecimal.ZERO;
			if (!details.isEmpty()) {
				avgPrice = details.stream().map(OrderDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(new BigDecimal(details.size()), BigDecimal.ROUND_HALF_UP);
			}
			return new CategoryStatistics(cat.getName(), totalRevenue, totalQuantity, maxPrice, minPrice, avgPrice);
		}).collect(Collectors.toList());
		model.addAttribute("categoryStats", categoryStats);

		// Thống kê khách hàng VIP
		List<Account> accounts = accountService.findAll();
		List<VipCustomerStatistics> vipStats = accounts.stream().map(acc -> {
			List<Order> orders = orderService.findByAccountId(acc.getId());
			BigDecimal totalSpent = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
			LocalDateTime firstOrderDate = orders.stream().map(Order::getOrderDate).min(LocalDateTime::compareTo)
					.orElse(null);
			LocalDateTime lastOrderDate = orders.stream().map(Order::getOrderDate).max(LocalDateTime::compareTo)
					.orElse(null);
			return new VipCustomerStatistics(acc.getUsername(), totalSpent, firstOrderDate, lastOrderDate);
		}).sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent())).limit(10).collect(Collectors.toList());
		model.addAttribute("vipStats", vipStats);

		return "statistics";
	}

	@RequestMapping("/admin/statistics/export/category")
	public void exportCategoryStatsToPDF(HttpServletResponse response) {
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=category-stats.pdf");

		try {
			Document document = new Document();
			PdfWriter.getInstance(document, response.getOutputStream());
			document.open();

			// Tạo bảng PDF với 6 cột
			PdfPTable table = new PdfPTable(6);
			table.addCell("Loại hàng");
			table.addCell("Tổng doanh thu");
			table.addCell("Tổng số lượng");
			table.addCell("Giá cao nhất");
			table.addCell("Giá thấp nhất");
			table.addCell("Giá trung bình");

			// Lấy dữ liệu thống kê cho loại hàng
			List<Category> categories = categoryService.findAll();
			List<CategoryStatistics> categoryStats = categories.stream().map(cat -> {
				List<OrderDetail> details = orderDetailService.findAll().stream().filter(
						od -> od.getProduct() != null && od.getProduct().getCategory().getId().equals(cat.getId()))
						.collect(Collectors.toList());
				BigDecimal totalRevenue = details.stream()
						.map(od -> od.getPrice().multiply(new BigDecimal(od.getQuantity())))
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				int totalQuantity = details.stream().mapToInt(OrderDetail::getQuantity).sum();
				BigDecimal maxPrice = details.stream().map(OrderDetail::getPrice).max(BigDecimal::compareTo)
						.orElse(BigDecimal.ZERO);
				BigDecimal minPrice = details.stream().map(OrderDetail::getPrice).min(BigDecimal::compareTo)
						.orElse(BigDecimal.ZERO);
				BigDecimal avgPrice = BigDecimal.ZERO;
				if (!details.isEmpty()) {
					avgPrice = details.stream().map(OrderDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(new BigDecimal(details.size()), BigDecimal.ROUND_HALF_UP);
				}
				return new CategoryStatistics(cat.getName(), totalRevenue, totalQuantity, maxPrice, minPrice, avgPrice);
			}).collect(Collectors.toList());

			// Thêm dữ liệu vào bảng PDF
			for (CategoryStatistics stat : categoryStats) {
				table.addCell(stat.getCategoryName());
				table.addCell(stat.getTotalRevenue().toString());
				table.addCell(String.valueOf(stat.getTotalQuantity()));
				table.addCell(stat.getMaxPrice().toString());
				table.addCell(stat.getMinPrice().toString());
				table.addCell(stat.getAvgPrice().toString());
			}

			document.add(new Paragraph("Thống kê Doanh thu theo Loại hàng"));
			document.add(table);
			document.close();
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/admin/statistics/export/vip")
	public void exportVipStatsToPDF(HttpServletResponse response) {
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=vip-stats.pdf");

		try {
			Document document = new Document();
			PdfWriter.getInstance(document, response.getOutputStream());
			document.open();

			// Tạo bảng PDF với 4 cột
			PdfPTable table = new PdfPTable(4);
			table.addCell("Tên khách hàng");
			table.addCell("Tổng tiền đã mua");
			table.addCell("Ngày mua đầu tiên");
			table.addCell("Ngày mua sau cùng");

			// Lấy dữ liệu thống kê khách hàng VIP
			List<Account> accounts = accountService.findAll();
			List<VipCustomerStatistics> vipStats = accounts.stream().map(acc -> {
				List<Order> orders = orderService.findByAccountId(acc.getId());
				BigDecimal totalSpent = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO,
						BigDecimal::add);
				LocalDateTime firstOrderDate = orders.stream().map(Order::getOrderDate).min(LocalDateTime::compareTo)
						.orElse(null);
				LocalDateTime lastOrderDate = orders.stream().map(Order::getOrderDate).max(LocalDateTime::compareTo)
						.orElse(null);
				return new VipCustomerStatistics(acc.getUsername(), totalSpent, firstOrderDate, lastOrderDate);
			}).sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent())).limit(10).collect(Collectors.toList());

			// Thêm dữ liệu vào bảng PDF
			for (VipCustomerStatistics vip : vipStats) {
				table.addCell(vip.getUsername());
				table.addCell(vip.getTotalSpent().toString());
				table.addCell(vip.getFirstOrderDate() != null ? vip.getFirstOrderDate().toString() : "");
				table.addCell(vip.getLastOrderDate() != null ? vip.getLastOrderDate().toString() : "");
			}

			document.add(new Paragraph("Thống kê Khách hàng VIP"));
			document.add(table);
			document.close();
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}
	}

	// Lớp thống kê cho Loại hàng
	public static class CategoryStatistics {
		private String categoryName;
		private BigDecimal totalRevenue;
		private int totalQuantity;
		private BigDecimal maxPrice;
		private BigDecimal minPrice;
		private BigDecimal avgPrice;

		public CategoryStatistics(String categoryName, BigDecimal totalRevenue, int totalQuantity, BigDecimal maxPrice,
				BigDecimal minPrice, BigDecimal avgPrice) {
			this.categoryName = categoryName;
			this.totalRevenue = totalRevenue;
			this.totalQuantity = totalQuantity;
			this.maxPrice = maxPrice;
			this.minPrice = minPrice;
			this.avgPrice = avgPrice;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public BigDecimal getTotalRevenue() {
			return totalRevenue;
		}

		public int getTotalQuantity() {
			return totalQuantity;
		}

		public BigDecimal getMaxPrice() {
			return maxPrice;
		}

		public BigDecimal getMinPrice() {
			return minPrice;
		}

		public BigDecimal getAvgPrice() {
			return avgPrice;
		}
	}

	// Lớp thống kê cho khách hàng VIP
	public static class VipCustomerStatistics {
		private String username;
		private BigDecimal totalSpent;
		private LocalDateTime firstOrderDate;
		private LocalDateTime lastOrderDate;

		public VipCustomerStatistics(String username, BigDecimal totalSpent, LocalDateTime firstOrderDate,
				LocalDateTime lastOrderDate) {
			this.username = username;
			this.totalSpent = totalSpent;
			this.firstOrderDate = firstOrderDate;
			this.lastOrderDate = lastOrderDate;
		}

		public String getUsername() {
			return username;
		}

		public BigDecimal getTotalSpent() {
			return totalSpent;
		}

		public LocalDateTime getFirstOrderDate() {
			return firstOrderDate;
		}

		public LocalDateTime getLastOrderDate() {
			return lastOrderDate;
		}
	}
}
