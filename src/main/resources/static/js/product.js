var app = angular.module("productApp", [], function($interpolateProvider) {
	$interpolateProvider.startSymbol('[[');
	$interpolateProvider.endSymbol(']]');
});

app.controller("ProductController", ['$scope', '$http', function($scope, $http) {
	$scope.products = [];
	$scope.item = {}; // Object for the product form
	$scope.error = "";
	$scope.page = 0;
	$scope.totalPages = 0;
	$scope.categories = [];

	$scope.loadProducts = function() {
		$http.get("/api/product?page=" + $scope.page + "&size=5")
			.then(function(response) {
				$scope.products = response.data.content;
				$scope.totalPages = response.data.totalPages;
			})
			.catch(function(error) {
				console.error("Error loading products:", error);
				$scope.error = error.data;
			});
	};

	$scope.resetForm = function() {
		$scope.item = {};
		$scope.error = "";
		var fileNameDisplay = document.getElementById('fileNameDisplay');
		if (fileNameDisplay) fileNameDisplay.textContent = "";
		var imagePreviewImg = document.getElementById('imagePreview');
		if (imagePreviewImg) {
			imagePreviewImg.src = "";
			imagePreviewImg.style.display = "none";
		}
		document.getElementById('productForm').reset();
	};

	$scope.createProduct = function() {
		if (!$scope.item.name || !$scope.item.price || !$scope.item.quantity || !$scope.item.categoryId) {
			$scope.error = "Các trường bắt buộc không được để trống!";
			return;
		}
		var formElement = document.getElementById('productForm');
		var formData = new FormData(formElement);

		// ✅ Thêm dòng này để gửi categoryId đúng kiểu Long
		formData.append("categoryId", $scope.item.categoryId.toString());

		$http.post("/api/product", formData, {
			transformRequest: angular.identity,
			headers: { 'Content-Type': undefined }
		}).then(function(response) {
			$scope.loadProducts();
			$scope.resetForm();
		}).catch(function(error) {
			console.error("Error creating product:", error);
			$scope.error = error.data;
		});
	};

	$scope.updateProduct = function() {
		if (!$scope.item.name || !$scope.item.price || !$scope.item.quantity || !$scope.item.categoryId) {
			$scope.error = "Các trường bắt buộc không được để trống!";
			return;
		}
		var formData = new FormData();
		formData.append("id", $scope.item.id);
		formData.append("name", $scope.item.name);
		formData.append("price", $scope.item.price);
		formData.append("quantity", $scope.item.quantity);
		formData.append("categoryId", $scope.item.categoryId.toString());

		var imageInput = document.getElementById('image');
		if (imageInput.files.length > 0) {
			formData.append("imageFile", imageInput.files[0]);
		}
		$http.put("/api/product", formData, {
			transformRequest: angular.identity,
			headers: { 'Content-Type': undefined }
		}).then(function(response) {
			$scope.loadProducts();
			$scope.resetForm();
		}).catch(function(error) {
			console.error("Error updating product:", error);
			$scope.error = error.data;
		});
	};

	$scope.deleteProduct = function(id) {
		$http.delete("/api/product/" + id)
			.then(function(response) {
				$scope.loadProducts();
			})
			.catch(function(error) {
				console.error("Error deleting product:", error);
				$scope.error = error.data;
			});
	};

	$scope.editProduct = function(prod) {
		$scope.item = angular.copy(prod);
		// Ensure the categoryId is a number
		$scope.item.categoryId = Number(prod.category.id);
		var imagePreviewImg = document.getElementById('imagePreview');
		if (prod.image) {
			imagePreviewImg.src = "/photos/" + prod.image;
			imagePreviewImg.style.display = "block";
		} else {
			imagePreviewImg.src = "";
			imagePreviewImg.style.display = "none";
		}
	};

	// Load categories for the dropdown
	$scope.loadCategories = function() {
		$http.get("/api/category")
			.then(function(response) {
				$scope.categories = response.data;
			})
			.catch(function(error) {
				console.error("Error loading categories:", error);
			});
	};

	$scope.goToPage = function(page) {
		if (page >= 0 && page < $scope.totalPages) {
			$scope.page = page;
			$scope.loadProducts();
		}
	};

	// Initialize controller by loading categories and products
	$scope.loadCategories();
	$scope.loadProducts();
}]);
