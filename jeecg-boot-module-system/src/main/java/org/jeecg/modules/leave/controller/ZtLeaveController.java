package org.jeecg.modules.leave.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.leave.entity.ZtLeave;
import org.jeecg.modules.leave.service.IZtLeaveService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description: 请假信息
 * @Author: lym
 * @Date:   2020-09-02
 * @Version: V1.0
 */
@Api(tags="请假信息")
@RestController
@RequestMapping("/leave/ztLeave")
@Slf4j
public class ZtLeaveController extends JeecgController<ZtLeave, IZtLeaveService> {
	@Autowired
	private IZtLeaveService ztLeaveService;
	
	/**
	 * 分页列表查询
	 *
	 * @param ztLeave
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "请假信息-分页列表查询")
	@ApiOperation(value="请假信息-分页列表查询", notes="请假信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(ZtLeave ztLeave,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
		QueryWrapper<ZtLeave> queryWrapper = QueryGenerator.initQueryWrapper(ztLeave, req.getParameterMap());
		Page<ZtLeave> page = new Page<ZtLeave>(pageNo, pageSize);
		IPage<ZtLeave> pageList = ztLeaveService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param ztLeave
	 * @return
	 */
	@AutoLog(value = "请假信息-添加")
	@ApiOperation(value="请假信息-添加", notes="请假信息-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody ZtLeave ztLeave) {
		ztLeaveService.save(ztLeave);
		return Result.ok("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param ztLeave
	 * @return
	 */
	@AutoLog(value = "请假信息-编辑")
	@ApiOperation(value="请假信息-编辑", notes="请假信息-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody ZtLeave ztLeave) {
		ztLeaveService.updateById(ztLeave);
		return Result.ok("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "请假信息-通过id删除")
	@ApiOperation(value="请假信息-通过id删除", notes="请假信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		ztLeaveService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "请假信息-批量删除")
	@ApiOperation(value="请假信息-批量删除", notes="请假信息-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.ztLeaveService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "请假信息-通过id查询")
	@ApiOperation(value="请假信息-通过id查询", notes="请假信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		ZtLeave ztLeave = ztLeaveService.getById(id);
		if(ztLeave==null) {
			return Result.error("未找到对应数据");
		}
		return Result.ok(ztLeave);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param ztLeave
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ZtLeave ztLeave) {
        return super.exportXls(request, ztLeave, ZtLeave.class, "请假信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ZtLeave.class);
    }

}
