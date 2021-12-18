package com.itheima.pinda.file.controller;

import com.itheima.pinda.base.BaseController;
import com.itheima.pinda.base.R;
import com.itheima.pinda.file.dto.AttachmentDTO;
import com.itheima.pinda.file.service.AttachmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author caoyifei
 * @create 2021/12/17 17:26
 */
@RestController
@RequestMapping("/attchmant")
@Slf4j
@Api(value = "附件", tags = "附件")
public class AttachmentController extends BaseController {

    @Resource
    private AttachmentService attachmentService;

    /**
     * 文件上传
     *
     * @param file     附件
     * @param bizId    业务id
     * @param id       文件id
     * @param bizType  业务类型
     * @param isSingle 是否单文件
     * @return R<AttachmentDTO>
     */
    @ApiOperation(value = "附件上传", notes = "附件上传")
    @PostMapping("/upload")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "isSingle", value = "是否单文件", dataType = "boolean", paramType = "query"),
                    @ApiImplicitParam(name = "id", value = "文件id", dataType = "long", paramType = "query"),
                    @ApiImplicitParam(name = "bizId", value = "业务id", dataType = "long", paramType = "query"),
                    @ApiImplicitParam(name = "bizType", value = "业务类型", dataType = "long", paramType = "query"),
                    @ApiImplicitParam(name = "file", value = "附件", dataType = "MultipartFile", allowMultiple = true, required = true),})
    public R<AttachmentDTO> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "bizId", required = false) Long bizId,
                                   @RequestParam(value = "id", required = false) Long id,
                                   @RequestParam(value = "bizType", required = false) String bizType,
                                   @RequestParam(value = "isSingle", required = false) Boolean isSingle) {
        if (file == null || file.isEmpty()) {
            this.fail("请求中未包含一个有效地文件");
        }
        // 执行文件上传逻辑
        AttachmentDTO attachmentDTO = attachmentService.upload(file, bizId, id, bizType, isSingle);
        return this.success(attachmentDTO);
    }


    /**
     * 文件上传
     *
     * @param ids 文件id
     * @return R<AttachmentDTO>
     */
    @ApiOperation(value = "删除文件", notes = "删除文件")
    @DeleteMapping
    @ApiImplicitParams({@ApiImplicitParam(name = "ids", value = "文件id", dataType = "List", paramType = "query")})
    public R<Boolean> remove(@RequestParam(value = "ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            this.fail("请求中未包含一个有效地id");
        }
        // 执行文件删除逻辑
       attachmentService.remove(ids);
        return this.success();
    }
}
