
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { annotationData } from '../../mocks/annotations';

export default function ImageAnnotationPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const imageId = searchParams.get('id') || 'IMG001';
  
  const [activeTab, setActiveTab] = useState<'measure' | 'tools' | 'shapes'>('measure');
  const [showMeasurePanel, setShowMeasurePanel] = useState(true);
  const [showToolPanel, setShowToolPanel] = useState(false);
  const [imageSettings, setImageSettings] = useState(annotationData.imageSettings);
  const [selectedTool, setSelectedTool] = useState<string | null>(null);
  const [zoomLevel, setZoomLevel] = useState(100);

  const handleSettingChange = (key: string, delta: number) => {
    setImageSettings(prev => ({
      ...prev,
      [key]: Math.max(0, Math.min(200, prev[key as keyof typeof prev] + delta))
    }));
  };

  return (
    <div className="min-h-screen bg-gray-900 flex flex-col">
      {/* 顶部导航栏 */}
      <div className="bg-gradient-to-r from-violet-700 via-purple-600 to-violet-700 px-4 py-3 flex items-center justify-between fixed top-0 left-0 right-0 z-50">
        <div className="flex items-center gap-3">
          <button 
            onClick={() => navigate(-1)}
            className="w-8 h-8 flex items-center justify-center bg-white/20 rounded-full"
          >
            <i className="ri-arrow-left-line text-white text-lg"></i>
          </button>
          <div>
            <h1 className="text-white font-semibold text-base">影像标注</h1>
            <p className="text-white/70 text-xs">影像ID: {imageId} | 患者: {annotationData.patientName}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button className="px-3 py-1.5 bg-white/20 rounded-lg text-white text-xs flex items-center gap-1">
            <i className="ri-save-line"></i>
            保存
          </button>
          <button className="px-3 py-1.5 bg-emerald-500 rounded-lg text-white text-xs flex items-center gap-1">
            <i className="ri-check-line"></i>
            完成
          </button>
        </div>
      </div>

      {/* 主内容区域 */}
      <div className="flex-1 pt-16 pb-14 flex flex-col">
        {/* 影像查看区域 */}
        <div className="flex-1 relative bg-black overflow-hidden">
          <div 
            className="absolute inset-0 flex items-center justify-center"
            style={{ filter: `contrast(${imageSettings.contrast}%) brightness(${100 + imageSettings.brightness}%)` }}
          >
            <img 
              src="https://readdy.ai/api/search-image?query=medical%20X-ray%20spine%20full%20body%20frontal%20view%20showing%20vertebral%20column%20with%20measurement%20lines%20and%20angle%20markers%20on%20dark%20background%20professional%20radiology%20image%20high%20contrast%20medical%20diagnostic%20quality&width=300&height=450&seq=spine001&orientation=portrait"
              alt="脊柱X光片"
              className="max-h-full object-contain"
              style={{ transform: `scale(${zoomLevel / 100})` }}
            />
            
            {/* 标注叠加层 */}
            <div className="absolute inset-0 pointer-events-none">
              <div className="absolute top-[15%] left-1/2 transform -translate-x-1/2">
                <div className="bg-cyan-500/80 px-2 py-0.5 rounded text-white text-xs whitespace-nowrap">TS: 11.4mm</div>
                <div className="border-t-2 border-dashed border-cyan-400 w-20 mt-1"></div>
              </div>
              <div className="absolute top-[22%] left-1/2 transform -translate-x-1/2">
                <div className="bg-green-500/80 px-2 py-0.5 rounded text-white text-xs whitespace-nowrap">RSH: 辅助标记</div>
              </div>
              <div className="absolute top-[38%] left-[35%]">
                <div className="bg-green-500/80 px-2 py-0.5 rounded text-white text-xs whitespace-nowrap mb-1">AVT: 12.9mm</div>
                <div className="bg-green-600/80 px-2 py-0.5 rounded text-white text-xs whitespace-nowrap">Cobb: 辅助标记</div>
              </div>
              <div className="absolute bottom-[18%] left-1/2 transform -translate-x-1/2">
                <div className="flex flex-col items-center gap-1">
                  <div className="bg-green-500/80 px-2 py-0.5 rounded text-white text-xs">Pelvic: 0.4°</div>
                  <div className="bg-green-500/80 px-2 py-0.5 rounded text-white text-xs">Sacral: 1.3°</div>
                </div>
              </div>
              <div className="absolute top-[20%] bottom-[20%] left-1/2 border-l-2 border-dashed border-green-400/60"></div>
            </div>
          </div>

          {/* 缩放控制 */}
          <div className="absolute bottom-4 left-4 flex items-center gap-2 bg-gray-800/80 rounded-lg px-3 py-2">
            <button onClick={() => setZoomLevel(prev => Math.max(50, prev - 10))} className="w-7 h-7 flex items-center justify-center bg-gray-700 rounded text-white">
              <i className="ri-subtract-line"></i>
            </button>
            <span className="text-white text-xs w-12 text-center">{zoomLevel}%</span>
            <button onClick={() => setZoomLevel(prev => Math.min(200, prev + 10))} className="w-7 h-7 flex items-center justify-center bg-gray-700 rounded text-white">
              <i className="ri-add-line"></i>
            </button>
          </div>

          {/* 快捷操作按钮 */}
          <div className="absolute top-4 right-4 flex flex-col gap-2">
            <button 
              onClick={() => setShowMeasurePanel(!showMeasurePanel)}
              className={`w-10 h-10 flex items-center justify-center rounded-lg ${showMeasurePanel ? 'bg-violet-500' : 'bg-gray-800/80'} text-white`}
            >
              <i className="ri-ruler-2-line text-lg"></i>
            </button>
            <button 
              onClick={() => setShowToolPanel(!showToolPanel)}
              className={`w-10 h-10 flex items-center justify-center rounded-lg ${showToolPanel ? 'bg-violet-500' : 'bg-gray-800/80'} text-white`}
            >
              <i className="ri-tools-line text-lg"></i>
            </button>
            <button className="w-10 h-10 flex items-center justify-center rounded-lg bg-gray-800/80 text-white">
              <i className="ri-fullscreen-line text-lg"></i>
            </button>
            <button className="w-10 h-10 flex items-center justify-center rounded-lg bg-gray-800/80 text-white">
              <i className="ri-refresh-line text-lg"></i>
            </button>
          </div>
        </div>

        {/* 测量数据面板 */}
        {showMeasurePanel && (
          <div className="bg-gray-800 border-t border-gray-700">
            <div className="px-4 py-2 flex items-center justify-between border-b border-gray-700">
              <div className="flex items-center gap-2">
                <i className="ri-list-check text-violet-400"></i>
                <span className="text-white text-sm font-medium">测量数据</span>
              </div>
              <button onClick={() => setShowMeasurePanel(false)} className="w-6 h-6 flex items-center justify-center text-gray-400">
                <i className="ri-arrow-down-s-line"></i>
              </button>
            </div>
            <div className="px-4 py-3 max-h-32 overflow-y-auto">
              <div className="space-y-2">
                {annotationData.measurements.map((item) => (
                  <div key={item.id} className="flex items-center justify-between bg-gray-700/50 rounded-lg px-3 py-2">
                    <div className="flex items-center gap-2">
                      <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }}></div>
                      <span className="text-white text-xs">{item.type}</span>
                    </div>
                    <span className="text-xs font-medium" style={{ color: item.color }}>{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* 工具面板 */}
        {showToolPanel && (
          <div className="bg-gray-800 border-t border-gray-700">
            <div className="px-4 py-2 flex items-center justify-between border-b border-gray-700">
              <div className="flex items-center gap-2">
                <i className="ri-tools-line text-violet-400"></i>
                <span className="text-white text-sm font-medium">测量工具</span>
              </div>
              <button onClick={() => setShowToolPanel(false)} className="w-6 h-6 flex items-center justify-center text-gray-400">
                <i className="ri-arrow-down-s-line"></i>
              </button>
            </div>
            
            <div className="px-4 py-2 flex gap-2 border-b border-gray-700">
              <button 
                onClick={() => setActiveTab('measure')}
                className={`px-3 py-1 rounded-full text-xs ${activeTab === 'measure' ? 'bg-violet-500 text-white' : 'bg-gray-700 text-gray-300'}`}
              >
                测量标注
              </button>
              <button 
                onClick={() => setActiveTab('shapes')}
                className={`px-3 py-1 rounded-full text-xs ${activeTab === 'shapes' ? 'bg-violet-500 text-white' : 'bg-gray-700 text-gray-300'}`}
              >
                辅助图形
              </button>
            </div>

            <div className="px-4 py-3">
              {activeTab === 'measure' && (
                <div className="grid grid-cols-4 gap-2">
                  {annotationData.tools.map((tool) => (
                    <button
                      key={tool.id}
                      onClick={() => setSelectedTool(tool.id)}
                      className={`flex flex-col items-center gap-1 p-2 rounded-lg ${selectedTool === tool.id ? 'bg-violet-500' : 'bg-gray-700/50'}`}
                    >
                      <div className="w-8 h-8 flex items-center justify-center">
                        <i className={`${tool.icon} text-white text-lg`}></i>
                      </div>
                      <span className="text-white text-[10px] whitespace-nowrap overflow-hidden text-ellipsis w-full text-center">{tool.name}</span>
                    </button>
                  ))}
                </div>
              )}
              
              {activeTab === 'shapes' && (
                <div className="grid grid-cols-5 gap-2">
                  {annotationData.shapes.map((shape) => (
                    <button
                      key={shape.id}
                      onClick={() => setSelectedTool(shape.id)}
                      className={`flex flex-col items-center gap-1 p-2 rounded-lg ${selectedTool === shape.id ? 'bg-violet-500' : 'bg-gray-700/50'}`}
                    >
                      <div className="w-8 h-8 flex items-center justify-center">
                        <i className={`${shape.icon} text-white text-lg`}></i>
                      </div>
                      <span className="text-white text-[10px]">{shape.name}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* 图像调节 */}
            <div className="px-4 py-3 border-t border-gray-700">
              <div className="flex items-center gap-2 mb-3">
                <i className="ri-contrast-2-line text-violet-400"></i>
                <span className="text-white text-xs">图像调节</span>
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-gray-400 text-xs w-12">对比</span>
                  <div className="flex items-center gap-2">
                    <button onClick={() => handleSettingChange('contrast', -10)} className="w-6 h-6 flex items-center justify-center bg-gray-700 rounded text-white text-xs">-</button>
                    <span className="text-white text-xs w-10 text-center">{imageSettings.contrast}%</span>
                    <button onClick={() => handleSettingChange('contrast', 10)} className="w-6 h-6 flex items-center justify-center bg-gray-700 rounded text-white text-xs">+</button>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-400 text-xs w-12">亮度</span>
                  <div className="flex items-center gap-2">
                    <button onClick={() => handleSettingChange('brightness', -10)} className="w-6 h-6 flex items-center justify-center bg-gray-700 rounded text-white text-xs">-</button>
                    <span className="text-white text-xs w-10 text-center">{imageSettings.brightness}</span>
                    <button onClick={() => handleSettingChange('brightness', 10)} className="w-6 h-6 flex items-center justify-center bg-gray-700 rounded text-white text-xs">+</button>
                  </div>
                </div>
              </div>
            </div>

            {/* 操作按钮 */}
            <div className="px-4 py-3 space-y-2">
              <button className="w-full py-2.5 bg-gradient-to-r from-emerald-500 to-teal-500 rounded-lg text-white text-sm font-medium flex items-center justify-center gap-2">
                <i className="ri-ai-generate"></i>
                AI智能识别
              </button>
              <button className="w-full py-2.5 bg-gradient-to-r from-orange-500 to-red-500 rounded-lg text-white text-sm font-medium flex items-center justify-center gap-2">
                <i className="ri-file-text-line"></i>
                生成报告
              </button>
            </div>
          </div>
        )}
      </div>

      {/* 底部操作栏 */}
      <div className="fixed bottom-0 left-0 right-0 bg-gray-800/95 backdrop-blur-lg border-t border-gray-700 px-4 py-2 flex items-center justify-between z-50">
        <div className="flex items-center gap-1">
          <button className="px-3 py-2 flex flex-col items-center">
            <i className="ri-drag-move-line text-gray-400 text-lg"></i>
            <span className="text-gray-400 text-[10px]">移动</span>
          </button>
          <button className="px-3 py-2 flex flex-col items-center">
            <i className="ri-zoom-in-line text-gray-400 text-lg"></i>
            <span className="text-gray-400 text-[10px]">缩放</span>
          </button>
        </div>
        <div className="flex items-center gap-1">
          <button className="px-3 py-2 flex flex-col items-center">
            <i className="ri-arrow-go-back-line text-gray-400 text-lg"></i>
            <span className="text-gray-400 text-[10px]">撤销</span>
          </button>
          <button className="px-3 py-2 flex flex-col items-center">
            <i className="ri-arrow-go-forward-line text-gray-400 text-lg"></i>
            <span className="text-gray-400 text-[10px]">重做</span>
          </button>
          <button className="px-3 py-2 flex flex-col items-center">
            <i className="ri-delete-bin-line text-red-400 text-lg"></i>
            <span className="text-red-400 text-[10px]">清除</span>
          </button>
        </div>
      </div>
    </div>
  );
}
