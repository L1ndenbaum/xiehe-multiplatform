
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { patientsData } from '../../mocks/patients';

export default function Patients() {
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState('');
  const [genderFilter, setGenderFilter] = useState('全部');
  const [showGenderFilter, setShowGenderFilter] = useState(false);

  const filteredPatients = patientsData.filter(patient => {
    const matchSearch = patient.name.includes(searchText) || 
                       patient.id.includes(searchText) || 
                       patient.phone.includes(searchText);
    const matchGender = genderFilter === '全部' || patient.gender === genderFilter;
    return matchSearch && matchGender;
  });

  return (
    <div className="flex flex-col h-full bg-gradient-to-b from-slate-50 to-slate-100">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-10 bg-gradient-to-r from-violet-600 via-purple-500 to-violet-600 px-4 pt-12 pb-6 flex-shrink-0">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-white text-xl font-bold">患者中心</h1>
            <p className="text-violet-100 text-xs mt-0.5">{filteredPatients.length} 位患者</p>
          </div>
          <button 
            onClick={() => navigate('/patients/add')}
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
            placeholder="搜索患者姓名、ID或手机号"
            className="w-full pl-11 pr-4 py-3 bg-white/95 backdrop-blur-sm border-none rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-white/50 shadow-lg shadow-violet-900/20 placeholder:text-slate-400"
          />
          <div className="absolute left-3.5 top-1/2 -translate-y-1/2 w-6 h-6 bg-slate-100 rounded-lg flex items-center justify-center">
            <i className="ri-search-line text-slate-500 text-sm"></i>
          </div>
        </div>
      </div>

      {/* 可滚动内容区域 */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        <div className="flex items-center gap-2 mb-4">
          <button
            onClick={() => setShowGenderFilter(true)}
            className={`px-4 py-2 rounded-full text-sm font-medium flex items-center gap-1.5 transition-all ${
              genderFilter !== '全部' 
                ? 'bg-violet-500 text-white shadow-md shadow-violet-500/30' 
                : 'bg-white text-slate-600 shadow-sm border border-slate-100'
            }`}
          >
            <i className="ri-filter-3-line text-sm"></i>
            <span>性别: {genderFilter}</span>
          </button>
        </div>

        <div className="space-y-3">
          {filteredPatients.map((patient) => (
            <div 
              key={patient.id} 
              className="bg-white rounded-2xl p-4 shadow-lg shadow-slate-200/50 border border-slate-100"
            >
              <div className="flex items-start">
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center mr-3 flex-shrink-0 bg-gradient-to-br ${
                  patient.gender === '男' ? 'from-violet-400 to-violet-500' : 'from-pink-400 to-pink-500'
                } shadow-md`}>
                  <i className={`${patient.gender === '男' ? 'ri-men-line' : 'ri-women-line'} text-white text-xl`}></i>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="text-base font-bold text-slate-800">{patient.name}</h3>
                    <span className={`px-2 py-0.5 text-[10px] font-semibold rounded-full ${
                      patient.gender === '男' ? 'bg-violet-50 text-violet-600' : 'bg-pink-50 text-pink-600'
                    }`}>
                      {patient.gender}
                    </span>
                  </div>
                  <div className="text-sm text-slate-500 mb-1.5">{patient.age}岁</div>
                  <div className="text-sm text-slate-600 flex items-center">
                    <div className="w-5 h-5 bg-slate-100 rounded-md flex items-center justify-center mr-1.5">
                      <i className="ri-phone-line text-slate-500 text-xs"></i>
                    </div>
                    {patient.phone}
                  </div>
                </div>
              </div>
              <div className="flex gap-2 mt-4 pt-3 border-t border-slate-100">
                <button
                  onClick={() => navigate(`/patients/edit/${patient.id}`)}
                  className="flex-1 py-2.5 bg-emerald-50 text-emerald-600 text-sm font-medium rounded-xl hover:bg-emerald-100 transition-colors flex items-center justify-center gap-1"
                >
                  <i className="ri-edit-line text-sm"></i>
                  编辑
                </button>
                <button
                  onClick={() => navigate(`/patients/detail/${patient.id}`)}
                  className="flex-1 py-2.5 bg-gradient-to-r from-violet-500 to-purple-500 text-white text-sm font-medium rounded-xl hover:shadow-lg hover:shadow-violet-500/30 transition-all flex items-center justify-center gap-1"
                >
                  <i className="ri-eye-line text-sm"></i>
                  查看
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 性别筛选弹窗 */}
      {showGenderFilter && (
        <div className="absolute inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-end" onClick={() => setShowGenderFilter(false)}>
          <div className="bg-white w-full rounded-t-3xl p-6 pb-8" onClick={(e) => e.stopPropagation()}>
            <div className="w-10 h-1 bg-slate-200 rounded-full mx-auto mb-5"></div>
            <h3 className="text-lg font-bold text-slate-800 mb-4">选择性别</h3>
            <div className="space-y-2">
              {['全部', '男', '女'].map((gender) => (
                <button
                  key={gender}
                  onClick={() => { setGenderFilter(gender); setShowGenderFilter(false); }}
                  className={`w-full py-3.5 rounded-xl text-sm font-medium transition-all ${
                    genderFilter === gender
                      ? 'bg-gradient-to-r from-violet-500 to-purple-500 text-white shadow-lg shadow-violet-500/30'
                      : 'bg-slate-50 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  {gender}
                </button>
              ))}
            </div>
            <button onClick={() => setShowGenderFilter(false)} className="w-full mt-4 py-3.5 bg-slate-100 text-slate-600 rounded-xl text-sm font-medium">取消</button>
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
        <button className="flex flex-col items-center justify-center py-1.5 relative">
          <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-purple-500 rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/30">
            <i className="ri-user-heart-fill text-white text-lg"></i>
          </div>
          <span className="text-[10px] mt-1 font-semibold text-violet-600">患者中心</span>
        </button>
        <button onClick={() => navigate('/images')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-image-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">影像中心</span>
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
