
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { imagesData } from '../../mocks/images';

export default function ImagesPage() {
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState('');
  const [typeFilter, setTypeFilter] = useState('全部类型');
  const [statusFilter, setStatusFilter] = useState('全部状态');
  const [showTypeFilter, setShowTypeFilter] = useState(false);
  const [showStatusFilter, setShowStatusFilter] = useState(false);

  const filteredImages = imagesData.filter(image => {
    const matchSearch = image.fileName.includes(searchText) || 
                       image.patientName.includes(searchText) ||
                       image.checkType.includes(searchText);
    const matchType = typeFilter === '全部类型' || image.checkType === typeFilter;
    const matchStatus = statusFilter === '全部状态' || image.status === statusFilter;
    return matchSearch && matchType && matchStatus;
  });

  const getTypeColor = (type: string) => {
    switch(type) {
      case '正面': return 'from-cyan-400 to-teal-500';
      case '侧面': return 'from-orange-400 to-amber-500';
      case '左侧曲位': return 'from-emerald-400 to-green-500';
      case '右侧曲位': return 'from-blue-400 to-sky-500';
      case '体态照片': return 'from-rose-400 to-pink-500';
      default: return 'from-slate-400 to-slate-500';
    }
  };

  return (
    <div className="flex flex-col h-full bg-gradient-to-b from-slate-50 to-slate-100">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-10 bg-gradient-to-r from-violet-600 via-purple-500 to-violet-600 px-4 pt-12 pb-6 flex-shrink-0">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-white text-xl font-bold">影像中心</h1>
            <p className="text-violet-100 text-xs mt-0.5">{filteredImages.length} 份影像</p>
          </div>
          <button 
            onClick={() => navigate('/images/upload')}
            className="w-10 h-10 flex items-center justify-center bg-white/20 backdrop-blur-sm rounded-full text-white"
          >
            <i className="ri-add-line text-xl"></i>
          </button>
        </div>
        <div className="relative">
          <input
            type="text"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            placeholder="搜索患者姓名、检查类型或文件名"
            className="w-full pl-11 pr-4 py-3 bg-white/95 backdrop-blur-sm border-none rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-white/50 shadow-lg shadow-violet-900/20 placeholder:text-slate-400"
          />
          <div className="absolute left-3.5 top-1/2 -translate-y-1/2 w-6 h-6 bg-slate-100 rounded-lg flex items-center justify-center">
            <i className="ri-search-line text-slate-500 text-sm"></i>
          </div>
        </div>
      </div>

      {/* 可滚动内容区域 */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        <div className="flex items-center gap-2 mb-4 overflow-x-auto pb-1">
          <button
            onClick={() => setShowTypeFilter(true)}
            className={`px-4 py-2 rounded-full text-sm font-medium flex items-center gap-1.5 transition-all whitespace-nowrap ${
              typeFilter !== '全部类型' 
                ? 'bg-violet-500 text-white shadow-md shadow-violet-500/30' 
                : 'bg-white text-slate-600 shadow-sm border border-slate-100'
            }`}
          >
            <i className="ri-file-list-3-line text-sm"></i>
            <span>{typeFilter}</span>
          </button>
          <button
            onClick={() => setShowStatusFilter(true)}
            className={`px-4 py-2 rounded-full text-sm font-medium flex items-center gap-1.5 transition-all whitespace-nowrap ${
              statusFilter !== '全部状态' 
                ? 'bg-emerald-500 text-white shadow-md shadow-emerald-500/30' 
                : 'bg-white text-slate-600 shadow-sm border border-slate-100'
            }`}
          >
            <i className="ri-checkbox-circle-line text-sm"></i>
            <span>{statusFilter}</span>
          </button>
        </div>

        <div className="space-y-3">
          {filteredImages.map((image) => (
            <div key={image.id} className="bg-white rounded-2xl p-4 shadow-lg shadow-slate-200/50 border border-slate-100">
              <div className="flex items-start mb-4">
                <div className={`w-14 h-14 rounded-xl flex items-center justify-center mr-3 flex-shrink-0 bg-gradient-to-br ${getTypeColor(image.checkType)} shadow-md`}>
                  <i className="ri-file-image-line text-white text-2xl"></i>
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="text-sm font-bold text-slate-800 mb-1.5 truncate">{image.fileName}</h3>
                  <div className="flex items-center gap-2 mb-2">
                    <span className={`px-2.5 py-1 text-[10px] font-semibold rounded-full bg-gradient-to-r ${getTypeColor(image.checkType)} text-white`}>
                      {image.checkType}
                    </span>
                    <span className={`px-2.5 py-1 text-[10px] font-semibold rounded-full ${
                      image.status === '已上传' ? 'bg-emerald-50 text-emerald-600' :
                      image.status === '处理中' ? 'bg-amber-50 text-amber-600' :
                      'bg-slate-50 text-slate-600'
                    }`}>
                      {image.status}
                    </span>
                  </div>
                  <div className="text-xs text-slate-400 flex items-center">
                    <i className="ri-time-line mr-1"></i>
                    {image.uploadTime}
                  </div>
                </div>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => navigate(`/image-annotation?id=${image.id}`)}
                  className="flex-1 py-2.5 bg-gradient-to-r from-violet-500 to-purple-500 text-white text-sm font-medium rounded-xl hover:shadow-lg hover:shadow-violet-500/30 transition-all flex items-center justify-center gap-1.5"
                >
                  <i className="ri-eye-line"></i>
                  <span>标注分析</span>
                </button>
                <button className="w-11 h-11 bg-slate-50 text-slate-500 rounded-xl hover:bg-slate-100 transition-colors flex items-center justify-center">
                  <i className="ri-download-line text-lg"></i>
                </button>
                <button className="w-11 h-11 bg-red-50 text-red-500 rounded-xl hover:bg-red-100 transition-colors flex items-center justify-center">
                  <i className="ri-delete-bin-line text-lg"></i>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 类型筛选弹窗 */}
      {showTypeFilter && (
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-end" onClick={() => setShowTypeFilter(false)}>
          <div className="bg-white w-full rounded-t-3xl p-6 pb-8" onClick={(e) => e.stopPropagation()}>
            <div className="w-10 h-1 bg-slate-200 rounded-full mx-auto mb-5"></div>
            <h3 className="text-lg font-bold text-slate-800 mb-4">选择检查类型</h3>
            <div className="space-y-2">
              {['全部类型', '正面', '侧面', '左侧曲位', '右侧曲位', '体态照片'].map((type) => (
                <button key={type} onClick={() => { setTypeFilter(type); setShowTypeFilter(false); }}
                  className={`w-full py-3.5 rounded-xl text-sm font-medium transition-all ${typeFilter === type ? 'bg-gradient-to-r from-violet-500 to-purple-500 text-white shadow-lg shadow-violet-500/30' : 'bg-slate-50 text-slate-700 hover:bg-slate-100'}`}>
                  {type}
                </button>
              ))}
            </div>
            <button onClick={() => setShowTypeFilter(false)} className="w-full mt-4 py-3.5 bg-slate-100 text-slate-600 rounded-xl text-sm font-medium">取消</button>
          </div>
        </div>
      )}

      {/* 状态筛选弹窗 */}
      {showStatusFilter && (
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-end" onClick={() => setShowStatusFilter(false)}>
          <div className="bg-white w-full rounded-t-3xl p-6 pb-8" onClick={(e) => e.stopPropagation()}>
            <div className="w-10 h-1 bg-slate-200 rounded-full mx-auto mb-5"></div>
            <h3 className="text-lg font-bold text-slate-800 mb-4">选择状态</h3>
            <div className="space-y-2">
              {['全部状态', '已上传', '处理中', '已完成'].map((status) => (
                <button key={status} onClick={() => { setStatusFilter(status); setShowStatusFilter(false); }}
                  className={`w-full py-3.5 rounded-xl text-sm font-medium transition-all ${statusFilter === status ? 'bg-gradient-to-r from-violet-500 to-purple-500 text-white shadow-lg shadow-violet-500/30' : 'bg-slate-50 text-slate-700 hover:bg-slate-100'}`}>
                  {status}
                </button>
              ))}
            </div>
            <button onClick={() => setShowStatusFilter(false)} className="w-full mt-4 py-3.5 bg-slate-100 text-slate-600 rounded-xl text-sm font-medium">取消</button>
          </div>
        </div>
      )}

      {/* 底部导航栏 */}
      <div className="sticky bottom-0 bg-white/90 backdrop-blur-lg border-t border-slate-100 px-2 py-2 grid grid-cols-4 z-30 flex-shrink-0">
        <button onClick={() => navigate('/dashboard')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-dashboard-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">工作台</span>
        </button>
        <button onClick={() => navigate('/patients')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-user-heart-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">患者中心</span>
        </button>
        <button className="flex flex-col items-center justify-center py-1.5 relative">
          <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-purple-500 rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/30">
            <i className="ri-image-fill text-white text-lg"></i>
          </div>
          <span className="text-[10px] mt-1 font-semibold text-violet-600">影像中心</span>
        </button>
        <button onClick={() => navigate('/profile')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-user-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">个人中心</span>
        </button>
      </div>
    </div>
  );
}
