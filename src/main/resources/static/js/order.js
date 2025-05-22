var app = angular.module("orderApp", [], function($interpolateProvider) {
	$interpolateProvider.startSymbol('[[');
	$interpolateProvider.endSymbol(']]');
});

app.controller("OrderController", ['$scope', '$http', function($scope, $http) {
	$scope.orders = [];         // Danh sách đơn hàng
	$scope.order = {};          // Dữ liệu cho form đơn hàng
	$scope.error = "";
	$scope.currentPage = 0;
	$scope.pageSize = 4;        // Số đơn hàng mỗi trang
	$scope.totalPages = 0;

	// Hàm tải danh sách đơn hàng với phân trang
	$scope.loadOrders = function() {
		$http.get("/api/orders?page=" + $scope.currentPage + "&size=" + $scope.pageSize)
			.then(function(response) {
				// API trả về Map với các thuộc tính content và totalPages
				$scope.orders = response.data.content;

				// Sắp xếp đơn hàng theo thời gian (mới nhất lên đầu bảng)
				$scope.orders = $scope.orders.sort(function(a, b) {
					return new Date(b.orderDate) - new Date(a.orderDate);
				});

				$scope.totalPages = response.data.totalPages;
			})
			.catch(function(error) {
				console.error("Error loading orders:", error);
				$scope.error = error.data;
			});
	};

	$scope.createOrder = function() {
		$scope.error = "Không thể thêm đơn hàng! Chỉ khách hàng mới được phép đặt.";
		return;
	};

	// Hàm cập nhật đơn hàng
	$scope.updateOrder = function() {
		if (!$scope.order.id) {
			$scope.error = "ID đơn hàng không hợp lệ!";
			return;
		}
		$http.put("/api/orders/" + $scope.order.id, $scope.order)
			.then(function(response) {
				$scope.loadOrders();
				$scope.resetForm();
			})
			.catch(function(error) {
				console.error("Error updating order:", error);
				$scope.error = error.data;
			});
	};

	// Hàm xóa đơn hàng
	$scope.deleteOrder = function(id) {
		if (confirm("Bạn có chắc chắn muốn xóa đơn hàng này?")) {
			$http.delete("/api/orders/" + id)
				.then(function(response) {
					$scope.loadOrders();
				})
				.catch(function(error) {
					console.error("Error deleting order:", error);
					$scope.error = error.data;
				});
		}
	};

	// Hàm chỉnh sửa đơn hàng: đưa dữ liệu vào form để sửa
	$scope.editOrder = function(o) {
		$scope.order = angular.copy(o);

		// Kiểm tra và chuyển đổi ngày đặt (orderDate) thành định dạng dd/MM/yyyy HH:mm
		if ($scope.order.orderDate && typeof $scope.order.orderDate === 'string') {
			// Chuyển đổi định dạng từ "yyyy-MM-ddTHH:mm" sang "dd/MM/yyyy HH:mm"
			const parts = $scope.order.orderDate.split("T"); // Tách ngày và giờ
			if (parts.length === 2) {
				const dateParts = parts[0].split("-"); // Tách ngày
				const timeParts = parts[1].split(":"); // Tách giờ

				if (dateParts.length === 3 && timeParts.length === 2) {
					// Định dạng lại thành "dd/MM/yyyy HH:mm"
					const formattedDate = `${dateParts[2]}/${dateParts[1]}/${dateParts[0]} ${timeParts[0]}:${timeParts[1]}`;
					$scope.order.orderDate = formattedDate;
				} else {
					console.error("Invalid date or time format:", $scope.order.orderDate);
				}
			} else {
				console.error("Invalid orderDate format:", $scope.order.orderDate);
			}
		} else {
			console.error("orderDate is not defined or not a string:", $scope.order.orderDate);
		}
	};

	// Hàm reset form
	$scope.resetForm = function() {
		$scope.order = {};
	};

	// Hàm chuyển trang
	$scope.goToPage = function(page) {
		if (page >= 0 && page < $scope.totalPages) {
			$scope.currentPage = page;
			$scope.loadOrders();
		}
	};

	// Hàm chuyển hướng xem chi tiết đơn hàng (chuyển sang trang chi tiết đơn hàng)
	$scope.viewOrderDetails = function(id) {
		window.location.href = "/admin/order/detail/" + id;
	};

	// Khởi tạo: tải danh sách đơn hàng
	$scope.loadOrders();
}]);
