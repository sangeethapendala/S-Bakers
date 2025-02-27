package com.project.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.dto.ProductDt;
import com.project.entity.Admin;
import com.project.entity.Category;
import com.project.entity.Product;
import com.project.service.AdminService;
import com.project.service.CategoryService;
import com.project.service.ProductService;

@Controller
public class AdminController {
	@Autowired
	private CategoryService cservice;
	@Autowired
	private ProductService pservice;
	@Autowired
	private AdminService aservice;
	public static String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/productImages";

	@GetMapping("/admin")
	public String admin() {
		return "login";
	}
	
	@RequestMapping("/register")
	public String register(String email,String password) {
		if(!(email==null && password==null)) {
			Admin a=new Admin();
			a.setEmail(email);
			a.setPassword(password);
			aservice.save(a);
		}
		return "register";
	}
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@PostMapping("/login")
	public String login(@RequestParam("email")String email,@RequestParam("password") String password,Model model) {
		List<Admin> list = aservice.fetchAll();
		for(Admin a:list) {
			if(a.getEmail().equals(email) && a.getPassword().equals(password)) {
				model.addAttribute("userobject", a);
				return "admin";
			}
		}
		return "login";
	}
	@GetMapping("/admin/categories")
	public String categorypage(Model model) {
		List<Category> list = cservice.getAll();
		model.addAttribute("categories", list);
		return "categories";
	}

	@GetMapping("/admin/categories/add")
	public String AddCategory(Model model) {
		Category c = new Category();
		model.addAttribute("category", c);
		return "categoriesAdd";
	}

	@PostMapping("/admin/categories/add")
	public String postAddCategory(@ModelAttribute("category") Category c) {
		cservice.saveCategory(c);
		return "redirect:/admin/categories";
	}

	@GetMapping("/admin/categories/delete/{id}")
	public String deleteCategory(@PathVariable("id") int id) {
		cservice.deletebyId(id);
		return "redirect:/admin/categories";

	}

	@GetMapping("/admin/categories/update/{id}")
	public String updateCategory(@PathVariable("id") int id, Model model) {
		Optional<Category> category = cservice.fetchbyId(id);
		if (category.isPresent()) {
			model.addAttribute("category", category.get());
			return "categoriesAdd";
		} else {
			return "error";

		}

	}

	@GetMapping("/admin/products")
	public String productPage(Model model) {
		List<Product> list = pservice.getAll();
		model.addAttribute("products", list);
		return "products";
	}

	@GetMapping("/admin/products/add")
	public String AddProduct(Model model) {

		ProductDt p = new ProductDt();
		model.addAttribute("productDTO", p);
		model.addAttribute("categories", cservice.getAll());
		return "productsAdd";

	}

	@PostMapping("/admin/products/add")
	public String postAddproduct(@ModelAttribute("productDt") ProductDt p,
			@RequestParam("productImage") MultipartFile file, @RequestParam("imgName") String imgName)
			throws IOException {
		Product pro = new Product();
		pro.setId(p.getId());
		pro.setName(p.getName());
		pro.setPrice(p.getPrice());
		pro.setDescription(p.getDescription());
		pro.setWeight(p.getWeight());
		pro.setCategory(cservice.fetchbyId(p.getCategoryId()).get());
		String imageUUID;
		if (!file.isEmpty()) {
			imageUUID = file.getOriginalFilename();
			Path path = Paths.get(uploadDir, imageUUID);
			Files.write(path, file.getBytes());
		} else {
			imageUUID = imgName;
		}
		pro.setImageName(imageUUID);
		pservice.saveProduct(pro);
		return "redirect:/admin/products";

	}

	@GetMapping("/admin/product/delete/{id}")
	public String deleteProduct(@PathVariable("id") long id) {
		pservice.deletebyId(id);
		return "redirect:/admin/products";
	}

	@GetMapping("/admin/product/update/{id}")
	public String updateProduct(@PathVariable("id") long id,Model model)
{
	Product pro=pservice.fetchbyId(id).get();
	ProductDt pdt=new ProductDt();
	pdt.setId(pro.getId());
	pdt.setName(pro.getName());
	pdt.setPrice(pro.getPrice());
	pdt.setWeight(pro.getWeight());
	pdt.setDescription(pro.getDescription());
	pdt.setCategoryId(pro.getCategory().getId());
	pdt.setImageName(pro.getImageName());
	model.addAttribute("categories",cservice.getAll());
	model.addAttribute("productDTO", pdt);
	return "productsAdd";
		
}
}