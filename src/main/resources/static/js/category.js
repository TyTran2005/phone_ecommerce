var app = angular.module("categoryApp", [], function($interpolateProvider) {
	$interpolateProvider.startSymbol('[[');
	$interpolateProvider.endSymbol(']]');
});

app.controller("CategoryController", ['$scope', '$http', function($scope, $http) {
	$scope.categories = [];
	$scope.item = {};  // Đối tượng cho form
	$scope.error = "";

	// Hàm load danh sách loại hàng từ RESTful API
	$scope.loadCategories = function() {
		$http.get("/api/category")
			.then(function(response) {
				$scope.categories = response.data;
			})
			.catch(function(error) {
				console.error("Error loading categories:", error);
				$scope.error = error.data;
			});
	};

	// Hàm reset form
	$scope.resetForm = function() {
		$scope.item = {};
		$scope.error = "";
		// Reset file input display
		var fileNameDisplay = document.getElementById('fileNameDisplay');
		if (fileNameDisplay) fileNameDisplay.textContent = "";
		var logoPreviewImg = document.getElementById('logoPreview');
		if (logoPreviewImg) {
			logoPreviewImg.src = "";
			logoPreviewImg.style.display = "none";
		}
		// Reset form element
		document.getElementById('categoryForm').reset();
	};

	// Hàm tạo mới loại hàng sử dụng FormData để upload file
	$scope.createCategory = function() {
		// Kiểm tra trường name không được để trống
		if (!$scope.item.name || $scope.item.name.trim() === "") {
			$scope.error = "Tên loại hàng không được để trống!";
			return;
		}
		var formElement = document.getElementById('categoryForm');
		var formData = new FormData(formElement);
		// Không cần append "name" nữa vì input đã có thuộc tính name
		$http.post("/api/category", formData, {
			transformRequest: angular.identity,
			headers: { 'Content-Type': undefined }
		}).then(function(response) {
			$scope.loadCategories();
			$scope.resetForm();
		}).catch(function(error) {
			console.error("Error creating category:", error);
			$scope.error = error.data;
		});
	};


	// Hàm cập nhật loại hàng sử dụng FormData để upload file (nếu có)
	$scope.updateCategory = function() {
		// Kiểm tra trường name không được để trống
		if (!$scope.item.name || $scope.item.name.trim() === "") {
			$scope.error = "Tên loại hàng không được để trống!";
			return;
		}
		var formElement = document.getElementById('categoryForm');
		var formData = new FormData(formElement);
		// Không cần append "name" nữa vì input đã có thuộc tính name
		$http.put("/api/category", formData, {
			transformRequest: angular.identity,
			headers: { 'Content-Type': undefined }
		}).then(function(response) {
			$scope.loadCategories();
			$scope.resetForm();
		}).catch(function(error) {
			console.error("Error updating category:", error);
			$scope.error = error.data;
		});
	};


	// Hàm xóa loại hàng (không hiển thị alert thông báo khi xóa)
	$scope.deleteCategory = function(id) {
		$http.delete("/api/category/" + id)
			.then(function(response) {
				$scope.loadCategories();
			})
			.catch(function(error) {
				console.error("Error deleting category:", error);
				$scope.error = error.data;
			});
	};

	// Hàm chuyển dữ liệu từ bảng sang form để sửa, và hiển thị preview logo
	$scope.editCategory = function(cat) {
		$scope.item = angular.copy(cat);
		// Nếu có logo, hiển thị preview
		if (cat.logo) {
			var logoPreviewImg = document.getElementById('logoPreview');
			if (logoPreviewImg) {
				logoPreviewImg.src = "/photos/" + cat.logo;
				logoPreviewImg.style.display = "block";
			}
		} else {
			var logoPreviewImg = document.getElementById('logoPreview');
			if (logoPreviewImg) {
				logoPreviewImg.src = "";
				logoPreviewImg.style.display = "none";
			}
		}
	};

	// Load danh sách loại hàng khi trang được load
	$scope.loadCategories();
}]);
