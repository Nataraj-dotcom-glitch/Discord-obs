/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useRef } from 'react';
import JSZip from 'jszip';
import {
  Video,
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  Play,
  Square,
  Pause,
  Settings,
  Folder,
  Plus,
  Trash2,
  Copy,
  Download,
  Eye,
  EyeOff,
  Lock,
  Unlock,
  Smartphone,
  RotateCw,
  Code2,
  CheckCircle2,
  AlertCircle,
  Camera,
  Layers,
  Sliders,
  ChevronRight,
  Monitor,
  Share2,
  FileCode,
  Shield,
  Zap,
  ArrowUp,
  ArrowDown
} from 'lucide-react';
import { ANDROID_PROJECT_FILES, AndroidFile } from './androidProjectFiles';

interface SceneItem {
  id: string;
  name: string;
  sources: SourceItem[];
}

interface SourceItem {
  id: string;
  name: string;
  type: 'screen' | 'camera' | 'mic' | 'device_audio' | 'text' | 'color';
  isEnabled: boolean;
  isLocked: boolean;
  isMuted?: boolean;
  volume?: number;
  x: number; // 0..1
  y: number; // 0..1
  w: number; // 0..1
  h: number; // 0..1
  text?: string;
  color?: string;
}

interface RecordingFile {
  id: string;
  name: string;
  date: string;
  duration: string;
  size: string;
  url?: string;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'code' | 'guide'>('simulator');
  const [simulatorView, setSimulatorView] = useState<'studio' | 'recordings' | 'settings' | 'firstlaunch'>('studio');
  const [orientation, setOrientation] = useState<'portrait' | 'landscape'>('portrait');
  const [isZipping, setIsZipping] = useState(false);
  const [selectedFile, setSelectedFile] = useState<AndroidFile>(ANDROID_PROJECT_FILES[0]);
  const [copiedCode, setCopiedCode] = useState(false);

  // Studio Simulator State
  const [scenes, setScenes] = useState<SceneItem[]>([
    {
      id: 'sc-1',
      name: 'Gaming Live',
      sources: [
        { id: 'src-1', name: 'Display Capture', type: 'screen', isEnabled: true, isLocked: true, x: 0, y: 0, w: 1, h: 1 },
        { id: 'src-2', name: 'Front Cam Overlay', type: 'camera', isEnabled: true, isLocked: false, x: 0.65, y: 0.08, w: 0.3, h: 0.28 },
        { id: 'src-3', name: 'Microphone', type: 'mic', isEnabled: true, isLocked: true, isMuted: false, volume: 1, x: 0, y: 0, w: 0, h: 0 },
        { id: 'src-4', name: 'Device Game Audio', type: 'device_audio', isEnabled: true, isLocked: true, isMuted: false, volume: 0.85, x: 0, y: 0, w: 0, h: 0 }
      ]
    },
    {
      id: 'sc-2',
      name: 'Facecam & Chat',
      sources: [
        { id: 'src-cam-full', name: 'Camera Fullscreen', type: 'camera', isEnabled: true, isLocked: true, x: 0, y: 0, w: 1, h: 1 },
        { id: 'src-txt', name: 'Stream Title', type: 'text', text: 'DARK ALISE OBS • LIVE', isEnabled: true, isLocked: false, x: 0.05, y: 0.05, w: 0.5, h: 0.1 },
        { id: 'src-mic-2', name: 'Studio Mic', type: 'mic', isEnabled: true, isLocked: true, isMuted: false, volume: 1, x: 0, y: 0, w: 0, h: 0 }
      ]
    },
    {
      id: 'sc-3',
      name: 'Pure Screen',
      sources: [
        { id: 'src-pure', name: 'Display Capture Only', type: 'screen', isEnabled: true, isLocked: true, x: 0, y: 0, w: 1, h: 1 }
      ]
    }
  ]);
  const [activeSceneId, setActiveSceneId] = useState<string>('sc-1');

  // Recording State
  const [recordingStatus, setRecordingStatus] = useState<'idle' | 'recording' | 'paused'>('idle');
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [micLevel, setMicLevel] = useState(0);
  const [deviceLevel, setDeviceLevel] = useState(0);
  const [recordings, setRecordings] = useState<RecordingFile[]>([
    { id: 'rec-01', name: 'DarkAliseOBS_rec_20261005_143022.mp4', date: 'Oct 05, 2026 14:30', duration: '05:42', size: '142.5 MB' },
    { id: 'rec-02', name: 'DarkAliseOBS_rec_20261005_181204.mp4', date: 'Oct 05, 2026 18:12', duration: '12:18', size: '318.2 MB' }
  ]);

  // Settings State
  const [resolution, setResolution] = useState<'720p' | '1080p'>('1080p');
  const [fps, setFps] = useState<30 | 60>(60);
  const [bitrate, setBitrate] = useState<'4M' | '8M' | '12M' | '16M'>('8M');
  const [encoder, setEncoder] = useState<'hardware' | 'software'>('hardware');
  const [recordMic, setRecordMic] = useState(true);
  const [recordDeviceAudio, setRecordDeviceAudio] = useState(true);
  const [micVolume, setMicVolume] = useState(1);
  const [deviceVolume, setDeviceVolume] = useState(0.85);

  const activeScene = scenes.find(s => s.id === activeSceneId) || scenes[0];

  // Dragging state for camera overlay on the preview
  const [camDragPos, setCamDragPos] = useState({ x: 0.65, y: 0.08 });
  const previewCanvasRef = useRef<HTMLDivElement>(null);

  // Timer loop
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (recordingStatus === 'recording') {
      interval = setInterval(() => {
        setElapsedSeconds(s => s + 1);
        // Dynamic simulated VU meter activity
        setMicLevel(Math.min(1, Math.max(0.1, Math.random() * 0.75 + (Math.sin(Date.now() / 300) > 0 ? 0.2 : 0))));
        setDeviceLevel(Math.min(1, Math.max(0.05, Math.random() * 0.6)));
      }, 1000);
    } else {
      setMicLevel(0);
      setDeviceLevel(0);
    }
    return () => clearInterval(interval);
  }, [recordingStatus]);

  const formatTimer = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  // Recording controls
  const handleStartRecording = () => {
    setElapsedSeconds(0);
    setRecordingStatus('recording');
  };

  const handlePauseRecording = () => {
    setRecordingStatus(recordingStatus === 'recording' ? 'paused' : 'recording');
  };

  const handleStopRecording = () => {
    const durationStr = formatTimer(elapsedSeconds);
    const timeCode = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);
    const newFile: RecordingFile = {
      id: `rec-${Date.now()}`,
      name: `DarkAliseOBS_rec_${timeCode}.mp4`,
      date: 'Just now',
      duration: durationStr,
      size: `${(Math.max(1, elapsedSeconds * 1.4)).toFixed(1)} MB`
    };
    setRecordings([newFile, ...recordings]);
    setRecordingStatus('idle');
    setElapsedSeconds(0);
  };

  // Download entire Android Project as a ZIP
  const handleDownloadProjectZip = async () => {
    try {
      setIsZipping(true);
      const zip = new JSZip();

      // Top-level files
      zip.file('settings.gradle.kts', ANDROID_PROJECT_FILES.find(f => f.path === 'settings.gradle.kts')?.content || '');
      zip.file('build.gradle.kts', ANDROID_PROJECT_FILES.find(f => f.path === 'build.gradle.kts')?.content || '');
      zip.file('gradle.properties', 'org.gradle.jvmargs=-Xmx2048m\\nandroid.useAndroidX=true\\nandroid.nonTransitiveRClass=true\\n');
      
      // Gradle wrapper
      const gradleFolder = zip.folder('gradle');
      gradleFolder?.file('libs.versions.toml', ANDROID_PROJECT_FILES.find(f => f.path === 'gradle/libs.versions.toml')?.content || '');
      const wrapperFolder = gradleFolder?.folder('wrapper');
      wrapperFolder?.file('gradle-wrapper.properties', 'distributionBase=GRADLE_USER_HOME\\ndistributionPath=wrapper/dists\\ndistributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip\\nnetworkTimeout=10000\\nvalidateDistributionUrl=true\\nzipStoreBase=GRADLE_USER_HOME\\nzipStorePath=wrapper/dists\\n');

      // App module
      const appFolder = zip.folder('app');
      appFolder?.file('build.gradle.kts', ANDROID_PROJECT_FILES.find(f => f.path === 'app/build.gradle.kts')?.content || '');
      appFolder?.file('proguard-rules.pro', '-keepclassmembers class * { @android.media.* <methods>; }\\n');

      // Manifest & Res
      const srcMain = appFolder?.folder('src')?.folder('main');
      srcMain?.file('AndroidManifest.xml', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/AndroidManifest.xml')?.content || '');
      
      const resValues = srcMain?.folder('res')?.folder('values');
      resValues?.file('strings.xml', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/res/values/strings.xml')?.content || '');
      resValues?.file('colors.xml', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/res/values/colors.xml')?.content || '');
      resValues?.file('themes.xml', '<resources><style name="Theme.DarkAliseOBS" parent="android:Theme.Material.NoActionBar" /></resources>');

      // Kotlin package: com/darkalise/obs
      const kotlinPackage = srcMain?.folder('java')?.folder('com')?.folder('darkalise')?.folder('obs');
      kotlinPackage?.file('MainActivity.kt', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/java/com/darkalise/obs/MainActivity.kt')?.content || '');
      
      const recorderFolder = kotlinPackage?.folder('recorder');
      recorderFolder?.file('MediaProjectionRecorder.kt', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/java/com/darkalise/obs/recorder/MediaProjectionRecorder.kt')?.content || '');
      recorderFolder?.file('AudioCaptureManager.kt', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/java/com/darkalise/obs/recorder/AudioCaptureManager.kt')?.content || '');
      recorderFolder?.file('MediaStoreManager.kt', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/java/com/darkalise/obs/recorder/MediaStoreManager.kt')?.content || '');

      const serviceFolder = kotlinPackage?.folder('service');
      serviceFolder?.file('ScreenRecordingService.kt', ANDROID_PROJECT_FILES.find(f => f.path === 'app/src/main/java/com/darkalise/obs/service/ScreenRecordingService.kt')?.content || '');

      // Generate blob
      const content = await zip.generateAsync({ type: 'blob' });
      const downloadUrl = URL.createObjectURL(content);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = 'DarkAliseOBS_Android_Native_Project.zip';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(downloadUrl);
    } catch (err) {
      console.error('Failed to create ZIP', err);
    } finally {
      setIsZipping(false);
    }
  };

  const copyCodeToClipboard = () => {
    navigator.clipboard.writeText(selectedFile.content);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  return (
    <div className="flex flex-col h-screen w-screen bg-[#07060c] text-[#e2e0ed] select-none font-sans overflow-hidden">
      {/* Top Application Bar */}
      <header className="h-14 border-b border-[#231f36] bg-[#0c0a14] px-4 flex items-center justify-between shrink-0 z-30">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#8b5cf6] to-[#4c1d95] p-0.5 flex items-center justify-center shadow-lg shadow-purple-950/40">
            <div className="w-full h-full bg-[#120f21] rounded-[6px] flex items-center justify-center relative">
              <div className="w-3.5 h-3.5 rounded-full bg-[#ef4444] animate-pulse shadow-sm shadow-red-500/50" />
            </div>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-extrabold text-sm sm:text-base tracking-wider text-white">
                DARK ALISE <span className="text-[#a78bfa]">OBS</span>
              </h1>
              <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-purple-950/70 border border-purple-800/40 text-purple-300">
                Native Android 10+
              </span>
            </div>
            <p className="text-[10px] text-[#86809c] hidden sm:block">
              Jetpack Compose • MediaProjection • MediaCodec • MediaMuxer • MediaStore
            </p>
          </div>
        </div>

        {/* Center Tabs */}
        <div className="flex items-center bg-[#151224] p-1 rounded-lg border border-[#2b2542]">
          <button
            onClick={() => setActiveTab('simulator')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
              activeTab === 'simulator'
                ? 'bg-[#8b5cf6] text-white shadow-md'
                : 'text-[#9b94b3] hover:text-white'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>Mobile Simulator</span>
          </button>
          <button
            onClick={() => setActiveTab('code')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
              activeTab === 'code'
                ? 'bg-[#8b5cf6] text-white shadow-md'
                : 'text-[#9b94b3] hover:text-white'
            }`}
          >
            <Code2 className="w-3.5 h-3.5" />
            <span>Android Project Code</span>
          </button>
          <button
            onClick={() => setActiveTab('guide')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
              activeTab === 'guide'
                ? 'bg-[#8b5cf6] text-white shadow-md'
                : 'text-[#9b94b3] hover:text-white'
            }`}
          >
            <Zap className="w-3.5 h-3.5" />
            <span>Build & APK Guide</span>
          </button>
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleDownloadProjectZip}
            disabled={isZipping}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white text-xs font-bold shadow-md shadow-purple-900/40 transition active:scale-95 disabled:opacity-50"
          >
            <Download className="w-3.5 h-3.5" />
            <span>{isZipping ? 'Bundling...' : 'Download Project (.ZIP)'}</span>
          </button>
        </div>
      </header>

      {/* Main Workspace Area */}
      <div className="flex-1 flex overflow-hidden">
        {activeTab === 'simulator' && (
          <div className="flex-1 flex flex-col md:flex-row bg-[#090810] overflow-hidden">
            {/* Simulator Control Toolbar */}
            <div className="w-full md:w-64 border-b md:border-b-0 md:border-r border-[#221d33] bg-[#0f0c1a] p-4 flex flex-col gap-4 shrink-0 overflow-y-auto">
              <div>
                <h3 className="text-xs font-bold uppercase tracking-wider text-purple-400 mb-1 flex items-center gap-1.5">
                  <Smartphone className="w-3.5 h-3.5" /> Device Profile
                </h3>
                <div className="bg-[#171427] border border-[#2b2540] rounded-lg p-2.5 text-xs">
                  <div className="font-semibold text-white">Samsung Galaxy A14 5G</div>
                  <div className="text-[11px] text-[#938ba8] mt-0.5">MediaTek Dimensity 700 / Mali-G57</div>
                  <div className="text-[10px] text-purple-300 mt-1 font-mono">FHD+ (1080x2408) • 90Hz • 5000mAh</div>
                  <div className="text-[10px] text-emerald-400 mt-0.5 font-mono">Android 14 One UI 6 (API 34)</div>
                </div>
              </div>

              {/* Orientation Switcher */}
              <div>
                <div className="text-xs font-semibold text-[#b5afcf] mb-1.5 flex items-center justify-between">
                  <span>Display Orientation</span>
                  <button
                    onClick={() => setOrientation(o => o === 'portrait' ? 'landscape' : 'portrait')}
                    className="p-1 rounded bg-[#201b33] hover:bg-[#2b2445] text-purple-300"
                    title="Toggle Orientation"
                  >
                    <RotateCw className="w-3 h-3" />
                  </button>
                </div>
                <div className="grid grid-cols-2 gap-1.5 bg-[#171427] p-1 rounded-lg border border-[#2b2540]">
                  <button
                    onClick={() => setOrientation('portrait')}
                    className={`py-1.5 text-xs font-medium rounded ${
                      orientation === 'portrait' ? 'bg-[#8b5cf6] text-white' : 'text-[#88819e] hover:text-white'
                    }`}
                  >
                    Portrait (Vertical)
                  </button>
                  <button
                    onClick={() => setOrientation('landscape')}
                    className={`py-1.5 text-xs font-medium rounded ${
                      orientation === 'landscape' ? 'bg-[#8b5cf6] text-white' : 'text-[#88819e] hover:text-white'
                    }`}
                  >
                    Landscape (Studio)
                  </button>
                </div>
              </div>

              {/* App Navigation Screen Switcher */}
              <div>
                <div className="text-xs font-semibold text-[#b5afcf] mb-1.5">Active App Screen</div>
                <div className="space-y-1">
                  <button
                    onClick={() => setSimulatorView('studio')}
                    className={`w-full text-left px-2.5 py-2 rounded-md text-xs font-medium flex items-center justify-between ${
                      simulatorView === 'studio' ? 'bg-[#292245] text-purple-300 font-bold border border-purple-600/40' : 'text-[#8f88a8] hover:bg-[#1a162b]'
                    }`}
                  >
                    <span className="flex items-center gap-2">
                      <Monitor className="w-3.5 h-3.5" /> OBS Main Studio
                    </span>
                    {recordingStatus === 'recording' && <span className="w-2 h-2 rounded-full bg-red-500 animate-ping" />}
                  </button>
                  <button
                    onClick={() => setSimulatorView('recordings')}
                    className={`w-full text-left px-2.5 py-2 rounded-md text-xs font-medium flex items-center justify-between ${
                      simulatorView === 'recordings' ? 'bg-[#292245] text-purple-300 font-bold border border-purple-600/40' : 'text-[#8f88a8] hover:bg-[#1a162b]'
                    }`}
                  >
                    <span className="flex items-center gap-2">
                      <Folder className="w-3.5 h-3.5" /> Recordings Library
                    </span>
                    <span className="text-[10px] font-mono text-purple-400">({recordings.length})</span>
                  </button>
                  <button
                    onClick={() => setSimulatorView('settings')}
                    className={`w-full text-left px-2.5 py-2 rounded-md text-xs font-medium flex items-center justify-between ${
                      simulatorView === 'settings' ? 'bg-[#292245] text-purple-300 font-bold border border-purple-600/40' : 'text-[#8f88a8] hover:bg-[#1a162b]'
                    }`}
                  >
                    <span className="flex items-center gap-2">
                      <Settings className="w-3.5 h-3.5" /> Studio Settings
                    </span>
                  </button>
                  <button
                    onClick={() => setSimulatorView('firstlaunch')}
                    className={`w-full text-left px-2.5 py-2 rounded-md text-xs font-medium flex items-center justify-between ${
                      simulatorView === 'firstlaunch' ? 'bg-[#292245] text-purple-300 font-bold border border-purple-600/40' : 'text-[#8f88a8] hover:bg-[#1a162b]'
                    }`}
                  >
                    <span className="flex items-center gap-2">
                      <Shield className="w-3.5 h-3.5" /> First Launch Permissions
                    </span>
                  </button>
                </div>
              </div>

              {/* Native Engine Telemetry */}
              <div className="mt-auto pt-3 border-t border-[#221d33] text-[11px] space-y-1.5 text-[#86809c]">
                <div className="flex justify-between font-mono">
                  <span>Target Codec:</span>
                  <span className="text-white">OMX.MTK.video.encoder.avc</span>
                </div>
                <div className="flex justify-between font-mono">
                  <span>Output Path:</span>
                  <span className="text-purple-300">Movies/DarkAliseOBS/</span>
                </div>
                <div className="flex justify-between font-mono">
                  <span>Surface PTS:</span>
                  <span className="text-emerald-400">Sync nanoTime()</span>
                </div>
              </div>
            </div>

            {/* Device Canvas Frame */}
            <div className="flex-1 flex items-center justify-center p-3 sm:p-6 bg-[#08070e] overflow-auto">
              <div
                className={`relative bg-[#0d0b17] border-[8px] sm:border-[10px] border-[#221e33] rounded-[36px] sm:rounded-[44px] shadow-2xl shadow-purple-950/30 flex flex-col overflow-hidden transition-all duration-300 ${
                  orientation === 'portrait'
                    ? 'w-[360px] sm:w-[390px] h-[720px] sm:h-[780px]'
                    : 'w-[740px] sm:w-[820px] h-[400px] sm:h-[450px]'
                }`}
              >
                {/* Galaxy A14 5G Infinity-V Notch & Speaker Grill */}
                <div className="absolute top-0 left-1/2 -translate-x-1/2 h-5 w-28 bg-[#221e33] rounded-b-xl flex items-center justify-center z-40">
                  <div className="w-8 h-1 bg-[#37314f] rounded-full mb-1" />
                  <div className="w-3 h-3 rounded-full bg-[#0d0b17] border border-[#3b3457] absolute bottom-0.5" />
                </div>

                {/* Android Status Bar */}
                <div className="h-6 bg-[#0c0a14] px-5 flex items-center justify-between text-[10px] font-mono text-[#8d86a6] shrink-0 z-30 pt-1">
                  <span>14:30</span>
                  <div className="flex items-center gap-2">
                    <span className="text-emerald-400 text-[9px]">5G</span>
                    <span>94%</span>
                  </div>
                </div>

                {/* Android Screen Body */}
                <div className="flex-1 flex flex-col bg-[#0a0912] overflow-hidden text-[#e0def2]">
                  {/* SCREEN 1: First Launch Permission Screen */}
                  {simulatorView === 'firstlaunch' && (
                    <div className="flex-1 p-5 flex flex-col justify-between overflow-y-auto">
                      <div className="text-center pt-6">
                        <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-purple-800 to-violet-600 p-0.5 mx-auto mb-3 shadow-lg shadow-purple-900/50">
                          <div className="w-full h-full bg-[#120f21] rounded-[14px] flex items-center justify-center">
                            <div className="w-6 h-6 rounded-full bg-[#ef4444]" />
                          </div>
                        </div>
                        <h2 className="text-lg font-black tracking-wider text-white">DARK ALISE OBS</h2>
                        <p className="text-xs text-[#a78bfa] font-medium mt-1">
                          Professional screen recording for Android
                        </p>
                      </div>

                      <div className="bg-[#13111f] border border-[#27223b] rounded-xl p-3 space-y-2.5 my-4">
                        <div className="text-[10px] font-bold uppercase tracking-wider text-[#797291] font-mono">
                          Required Permissions
                        </div>
                        <div className="flex items-center justify-between bg-[#19152b] p-2 rounded-lg">
                          <div className="flex items-center gap-2 text-xs">
                            <Mic className="w-4 h-4 text-emerald-400" />
                            <div>
                              <div className="font-semibold text-white">Microphone</div>
                              <div className="text-[10px] text-[#7f7899]">Audio commentary</div>
                            </div>
                          </div>
                          <span className="text-[10px] font-bold text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">Granted</span>
                        </div>

                        <div className="flex items-center justify-between bg-[#19152b] p-2 rounded-lg">
                          <div className="flex items-center gap-2 text-xs">
                            <Camera className="w-4 h-4 text-emerald-400" />
                            <div>
                              <div className="font-semibold text-white">Camera</div>
                              <div className="text-[10px] text-[#7f7899]">Facecam overlay</div>
                            </div>
                          </div>
                          <span className="text-[10px] font-bold text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">Granted</span>
                        </div>

                        <div className="p-2 rounded-lg bg-[#151124] border border-purple-900/30 text-[10px] text-[#a098bd]">
                          ℹ️ MediaProjection screen capture consent dialog will be initiated by Android when you hit Record.
                        </div>
                      </div>

                      <button
                        onClick={() => setSimulatorView('studio')}
                        className="w-full py-3 bg-[#8b5cf6] hover:bg-[#7c3aed] text-white rounded-xl font-bold text-xs tracking-wide shadow-lg shadow-purple-950/50"
                      >
                        Enter Studio Workspace
                      </button>
                    </div>
                  )}

                  {/* SCREEN 2: Recordings Library */}
                  {simulatorView === 'recordings' && (
                    <div className="flex-1 flex flex-col overflow-hidden">
                      <div className="h-11 bg-[#131021] border-b border-[#231d36] px-3 flex items-center justify-between shrink-0">
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => setSimulatorView('studio')}
                            className="p-1 hover:bg-[#201a36] rounded text-[#8f87a8]"
                          >
                            ←
                          </button>
                          <span className="font-bold text-xs text-white tracking-wide">RECORDINGS</span>
                        </div>
                        <span className="text-[10px] font-mono text-purple-400">Movies/DarkAliseOBS/</span>
                      </div>

                      <div className="flex-1 p-3 overflow-y-auto space-y-2">
                        {recordings.map((rec) => (
                          <div
                            key={rec.id}
                            className="bg-[#141121] border border-[#27213b] rounded-lg p-2.5 flex items-center justify-between hover:border-purple-600/40 transition"
                          >
                            <div className="min-w-0 pr-2">
                              <div className="text-xs font-semibold text-white truncate">{rec.name}</div>
                              <div className="text-[10px] text-[#7e7799] mt-0.5 flex items-center gap-2">
                                <span>{rec.date}</span>
                                <span>•</span>
                                <span className="text-purple-300 font-mono">{rec.size}</span>
                                <span>•</span>
                                <span className="font-mono">{rec.duration}</span>
                              </div>
                            </div>
                            <div className="flex items-center gap-1 shrink-0">
                              <button
                                onClick={() => alert(`Playing ${rec.name}`)}
                                className="p-1.5 rounded bg-purple-950/60 hover:bg-purple-900/80 text-purple-300 border border-purple-800/40"
                                title="Play"
                              >
                                <Play className="w-3 h-3" />
                              </button>
                              <button
                                onClick={() => setRecordings(recordings.filter(r => r.id !== rec.id))}
                                className="p-1.5 rounded bg-red-950/60 hover:bg-red-900/80 text-red-300 border border-red-800/40"
                                title="Delete"
                              >
                                <Trash2 className="w-3 h-3" />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* SCREEN 3: Settings Screen */}
                  {simulatorView === 'settings' && (
                    <div className="flex-1 flex flex-col overflow-hidden">
                      <div className="h-11 bg-[#131021] border-b border-[#231d36] px-3 flex items-center justify-between shrink-0">
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => setSimulatorView('studio')}
                            className="p-1 hover:bg-[#201a36] rounded text-[#8f87a8]"
                          >
                            ←
                          </button>
                          <span className="font-bold text-xs text-white tracking-wide">SETTINGS</span>
                        </div>
                        <span className="text-[10px] text-purple-400 font-mono">v1.0.0</span>
                      </div>

                      <div className="flex-1 p-3 overflow-y-auto space-y-3">
                        {/* Video */}
                        <div className="bg-[#141121] border border-[#27213b] rounded-lg p-2.5 space-y-2">
                          <div className="text-[10px] font-bold text-purple-400 uppercase tracking-wider font-mono">Video Encoder</div>
                          <div>
                            <div className="text-[11px] text-[#938ba8] mb-1">Resolution</div>
                            <div className="grid grid-cols-2 gap-1.5 text-xs">
                              {(['720p', '1080p'] as const).map(res => (
                                <button
                                  key={res}
                                  onClick={() => setResolution(res)}
                                  className={`py-1 rounded font-medium ${resolution === res ? 'bg-purple-600 text-white' : 'bg-[#1b172e] text-[#86809c]'}`}
                                >
                                  {res === '720p' ? '720p HD' : '1080p FHD'}
                                </button>
                              ))}
                            </div>
                          </div>

                          <div>
                            <div className="text-[11px] text-[#938ba8] mb-1">Frame Rate</div>
                            <div className="grid grid-cols-2 gap-1.5 text-xs">
                              {([30, 60] as const).map(f => (
                                <button
                                  key={f}
                                  onClick={() => setFps(f)}
                                  className={`py-1 rounded font-medium ${fps === f ? 'bg-purple-600 text-white' : 'bg-[#1b172e] text-[#86809c]'}`}
                                >
                                  {f} FPS
                                </button>
                              ))}
                            </div>
                          </div>

                          <div>
                            <div className="text-[11px] text-[#938ba8] mb-1">Bitrate</div>
                            <div className="grid grid-cols-4 gap-1 text-[11px] font-mono">
                              {(['4M', '8M', '12M', '16M'] as const).map(b => (
                                <button
                                  key={b}
                                  onClick={() => setBitrate(b)}
                                  className={`py-1 rounded ${bitrate === b ? 'bg-purple-600 text-white font-bold' : 'bg-[#1b172e] text-[#86809c]'}`}
                                >
                                  {b}
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>

                        {/* Audio Settings */}
                        <div className="bg-[#141121] border border-[#27213b] rounded-lg p-2.5 space-y-2 text-xs">
                          <div className="text-[10px] font-bold text-purple-400 uppercase tracking-wider font-mono">Audio Settings</div>
                          <div className="flex items-center justify-between">
                            <span>Record Microphone</span>
                            <input
                              type="checkbox"
                              checked={recordMic}
                              onChange={e => setRecordMic(e.target.checked)}
                              className="accent-purple-600"
                            />
                          </div>
                          <div className="flex items-center justify-between">
                            <span>Record Device Audio</span>
                            <input
                              type="checkbox"
                              checked={recordDeviceAudio}
                              onChange={e => setRecordDeviceAudio(e.target.checked)}
                              className="accent-purple-600"
                            />
                          </div>
                          <div className="text-[10px] text-[#7d7596] pt-1">
                            Sample Rate: 48,000 Hz • Bitrate: 192 kbps AAC
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* SCREEN 4: Main Studio Workspace */}
                  {simulatorView === 'studio' && (
                    <div className="flex-1 flex flex-col overflow-hidden">
                      {/* Studio Header */}
                      <div className="h-10 bg-[#120f21] border-b border-[#241e38] px-3 flex items-center justify-between shrink-0">
                        <div className="flex items-center gap-1.5">
                          <div className="w-2 h-2 rounded-full bg-red-500" />
                          <span className="font-extrabold text-xs tracking-wider text-white">DARK ALISE</span>
                          <span className="text-[10px] text-purple-400 font-bold">OBS</span>
                        </div>

                        {/* Top Telemetry */}
                        <div className="flex items-center gap-2 text-[10px] font-mono">
                          <span className={`px-1.5 py-0.5 rounded font-bold ${
                            recordingStatus === 'recording'
                              ? 'bg-red-950 text-red-400 border border-red-800'
                              : recordingStatus === 'paused'
                              ? 'bg-amber-950 text-amber-400 border border-amber-800'
                              : 'bg-[#1b172e] text-[#7b7594]'
                          }`}>
                            {recordingStatus === 'recording' ? `REC ${formatTimer(elapsedSeconds)}` : recordingStatus === 'paused' ? 'PAUSED' : 'STANDBY'}
                          </span>
                          <span className="text-emerald-400 font-bold">{fps} FPS</span>
                          <span className="text-[#8e87aa] hidden sm:inline">14% CPU</span>
                        </div>

                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => setSimulatorView('recordings')}
                            className="p-1 hover:bg-[#201a36] rounded text-[#8f87a8]"
                            title="Recordings"
                          >
                            <Folder className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setSimulatorView('settings')}
                            className="p-1 hover:bg-[#201a36] rounded text-[#8f87a8]"
                            title="Settings"
                          >
                            <Settings className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {/* Main Studio Body: Live Preview & Panels */}
                      <div className={`flex-1 flex ${orientation === 'landscape' ? 'flex-row' : 'flex-col'} overflow-hidden`}>
                        {/* Live Preview Area */}
                        <div className={`${orientation === 'landscape' ? 'w-3/5' : 'h-44 sm:h-52'} bg-[#07060d] border-b ${orientation === 'landscape' ? 'border-r border-b-0' : ''} border-[#221c36] relative flex flex-col justify-center items-center overflow-hidden shrink-0`}>
                          {/* 16:9 Canvas Viewport */}
                          <div
                            ref={previewCanvasRef}
                            className="w-full h-full relative overflow-hidden bg-gradient-to-br from-[#120f24] to-[#0a0814] flex items-center justify-center"
                          >
                            {/* Screen capture background simulation */}
                            <div className="absolute inset-0 flex items-center justify-center opacity-85">
                              <div className="w-full h-full p-3 flex flex-col justify-between bg-[#151229]">
                                <div className="flex justify-between items-center text-[10px] text-purple-300 font-mono">
                                  <span>SCREEN: {resolution} @ {fps}fps</span>
                                  <span>BITRATE: {bitrate}</span>
                                </div>
                                <div className="text-center">
                                  <div className="text-xs font-bold text-white tracking-wide">
                                    [ Samsung Galaxy A14 5G Display Mirror ]
                                  </div>
                                  <div className="text-[9px] text-[#766f8e] mt-0.5">
                                    MediaProjection VirtualDisplay active
                                  </div>
                                </div>
                                <div className="h-1.5 w-full bg-[#201b3b] rounded-full overflow-hidden">
                                  <div className="h-full bg-gradient-to-r from-purple-500 to-indigo-500 w-2/3 animate-pulse" />
                                </div>
                              </div>
                            </div>

                            {/* Front Camera Overlay layer */}
                            {activeScene.sources.some(s => s.type === 'camera' && s.isEnabled) && (
                              <div
                                style={{
                                  left: `${camDragPos.x * 100}%`,
                                  top: `${camDragPos.y * 100}%`,
                                }}
                                className="absolute w-20 h-24 rounded-xl border-2 border-purple-500 bg-black/90 shadow-xl overflow-hidden cursor-move z-20 flex flex-col items-center justify-center"
                                onMouseDown={(e) => {
                                  const startX = e.clientX;
                                  const startY = e.clientY;
                                  const initialX = camDragPos.x;
                                  const initialY = camDragPos.y;
                                  const onMouseMove = (ev: MouseEvent) => {
                                    const dx = (ev.clientX - startX) / 250;
                                    const dy = (ev.clientY - startY) / 180;
                                    setCamDragPos({
                                      x: Math.max(0, Math.min(0.75, initialX + dx)),
                                      y: Math.max(0, Math.min(0.7, initialY + dy))
                                    });
                                  };
                                  const onMouseUp = () => {
                                    window.removeEventListener('mousemove', onMouseMove);
                                    window.removeEventListener('mouseup', onMouseUp);
                                  };
                                  window.addEventListener('mousemove', onMouseMove);
                                  window.addEventListener('mouseup', onMouseUp);
                                }}
                              >
                                <Camera className="w-5 h-5 text-purple-400 mb-1" />
                                <span className="text-[8px] font-bold text-white uppercase tracking-wider">Facecam</span>
                                <span className="text-[7px] text-[#9b93b8]">Drag to move</span>
                              </div>
                            )}

                            {/* Title watermark */}
                            <div className="absolute top-2 left-2 bg-black/70 backdrop-blur-sm px-2 py-0.5 rounded border border-purple-900/40 text-[9px] font-mono text-white font-bold z-10">
                              {activeScene.name.toUpperCase()} • PROGRAM
                            </div>
                          </div>
                        </div>

                        {/* Panels & Controls Area */}
                        <div className="flex-1 flex flex-col bg-[#0b0914] overflow-y-auto p-2.5 gap-2.5">
                          {/* Recording Controls Strip */}
                          <div className="bg-[#131021] border border-[#27213b] rounded-lg p-2 flex items-center justify-between gap-2 shrink-0">
                            {recordingStatus === 'idle' ? (
                              <button
                                onClick={handleStartRecording}
                                className="flex-1 py-2 rounded-md bg-[#ef4444] hover:bg-[#dc2626] text-white font-bold text-xs flex items-center justify-center gap-1.5 shadow-md shadow-red-950/40 active:scale-95 transition"
                              >
                                <div className="w-2.5 h-2.5 rounded-full bg-white" />
                                <span>Start Recording</span>
                              </button>
                            ) : (
                              <>
                                <button
                                  onClick={handlePauseRecording}
                                  className="flex-1 py-2 rounded-md bg-[#251f38] hover:bg-[#312a4a] text-white font-semibold text-xs flex items-center justify-center gap-1 border border-[#3b3254]"
                                >
                                  {recordingStatus === 'paused' ? <Play className="w-3 h-3 text-emerald-400" /> : <Pause className="w-3 h-3 text-amber-400" />}
                                  <span>{recordingStatus === 'paused' ? 'Resume' : 'Pause'}</span>
                                </button>
                                <button
                                  onClick={handleStopRecording}
                                  className="flex-1 py-2 rounded-md bg-[#b91c1c] hover:bg-[#991b1b] text-white font-bold text-xs flex items-center justify-center gap-1 shadow-md shadow-red-950/40"
                                >
                                  <Square className="w-3 h-3 fill-current" />
                                  <span>Stop</span>
                                </button>
                              </>
                            )}
                          </div>

                          {/* Audio Mixer Strip */}
                          <div className="bg-[#131021] border border-[#27213b] rounded-lg p-2.5 space-y-2">
                            <div className="flex items-center justify-between text-[10px] font-bold tracking-wider font-mono text-white">
                              <span>AUDIO MIXER</span>
                              <span className="text-purple-400">48kHz STEREO</span>
                            </div>

                            {/* Mic Channel */}
                            <div className="bg-[#181429] p-2 rounded-md space-y-1">
                              <div className="flex items-center justify-between text-[11px]">
                                <span className="font-semibold text-white flex items-center gap-1">
                                  <Mic className="w-3 h-3 text-emerald-400" /> Mic Channel
                                </span>
                                <span className="font-mono text-[10px] text-[#8e87aa]">{Math.round(micVolume * 100)}%</span>
                              </div>
                              {/* VU meter */}
                              <div className="h-1.5 w-full bg-[#27213d] rounded-full overflow-hidden flex">
                                <div
                                  style={{ width: `${Math.min(70, micLevel * 100)}%` }}
                                  className="h-full bg-emerald-500 transition-all duration-75"
                                />
                                {micLevel > 0.7 && (
                                  <div
                                    style={{ width: `${Math.min(20, (micLevel - 0.7) * 100)}%` }}
                                    className="h-full bg-amber-400"
                                  />
                                )}
                                {micLevel > 0.9 && (
                                  <div
                                    style={{ width: `${Math.min(10, (micLevel - 0.9) * 100)}%` }}
                                    className="h-full bg-red-500"
                                  />
                                )}
                              </div>
                              <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.05"
                                value={micVolume}
                                onChange={e => setMicVolume(parseFloat(e.target.value))}
                                className="w-full accent-purple-500 h-1 bg-[#251f3b] rounded-lg cursor-pointer"
                              />
                            </div>

                            {/* Device Audio Channel */}
                            <div className="bg-[#181429] p-2 rounded-md space-y-1">
                              <div className="flex items-center justify-between text-[11px]">
                                <span className="font-semibold text-white flex items-center gap-1">
                                  <Volume2 className="w-3 h-3 text-emerald-400" /> Device Game Audio
                                </span>
                                <span className="font-mono text-[10px] text-[#8e87aa]">{Math.round(deviceVolume * 100)}%</span>
                              </div>
                              {/* VU meter */}
                              <div className="h-1.5 w-full bg-[#27213d] rounded-full overflow-hidden flex">
                                <div
                                  style={{ width: `${Math.min(70, deviceLevel * 100)}%` }}
                                  className="h-full bg-emerald-500 transition-all duration-75"
                                />
                                {deviceLevel > 0.7 && (
                                  <div
                                    style={{ width: `${Math.min(20, (deviceLevel - 0.7) * 100)}%` }}
                                    className="h-full bg-amber-400"
                                  />
                                )}
                              </div>
                              <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.05"
                                value={deviceVolume}
                                onChange={e => setDeviceVolume(parseFloat(e.target.value))}
                                className="w-full accent-purple-500 h-1 bg-[#251f3b] rounded-lg cursor-pointer"
                              />
                            </div>

                            {/* Mandatory Android Audio Policy Restriction Warning Box */}
                            <div className="bg-[#241318] border border-[#4d1f2b] p-2 rounded text-[10px] text-[#fca5a5] flex items-start gap-1.5 leading-tight">
                              <AlertCircle className="w-3.5 h-3.5 text-red-400 shrink-0 mt-0.5" />
                              <span>Device audio cannot be captured because Android or the source app does not allow playback capture.</span>
                            </div>
                          </div>

                          {/* Scenes Selector */}
                          <div className="bg-[#131021] border border-[#27213b] rounded-lg p-2.5">
                            <div className="flex items-center justify-between text-[10px] font-bold uppercase tracking-wider font-mono text-white mb-2">
                              <span>SCENES</span>
                              <button
                                onClick={() => {
                                  const name = prompt('Enter scene name:', `Scene ${scenes.length + 1}`);
                                  if (name) {
                                    const newSc: SceneItem = {
                                      id: `sc-${Date.now()}`,
                                      name,
                                      sources: [{ id: `src-${Date.now()}`, name: 'Display Capture', type: 'screen', isEnabled: true, isLocked: true, x: 0, y: 0, w: 1, h: 1 }]
                                    };
                                    setScenes([...scenes, newSc]);
                                    setActiveSceneId(newSc.id);
                                  }
                                }}
                                className="p-0.5 rounded bg-purple-950 hover:bg-purple-900 text-purple-300"
                              >
                                <Plus className="w-3.5 h-3.5" />
                              </button>
                            </div>
                            <div className="grid grid-cols-3 gap-1.5">
                              {scenes.map(s => (
                                <button
                                  key={s.id}
                                  onClick={() => setActiveSceneId(s.id)}
                                  className={`p-1.5 rounded text-left truncate text-[11px] font-medium border ${
                                    s.id === activeSceneId
                                      ? 'bg-purple-900/60 border-purple-500 text-white'
                                      : 'bg-[#181429] border-transparent text-[#8881a1] hover:text-white'
                                  }`}
                                >
                                  {s.name}
                                </button>
                              ))}
                            </div>
                          </div>

                          {/* Sources Panel */}
                          <div className="bg-[#131021] border border-[#27213b] rounded-lg p-2.5">
                            <div className="flex items-center justify-between text-[10px] font-bold uppercase tracking-wider font-mono text-white mb-2">
                              <span>SOURCES ({activeScene.sources.length})</span>
                              <button
                                onClick={() => {
                                  const type = prompt('Add Source (screen, camera, text, color):', 'camera');
                                  if (type && ['screen', 'camera', 'text', 'color'].includes(type)) {
                                    const updated = scenes.map(s => {
                                      if (s.id === activeSceneId) {
                                        return {
                                          ...s,
                                          sources: [
                                            ...s.sources,
                                            {
                                              id: `src-${Date.now()}`,
                                              name: type === 'camera' ? 'Facecam' : type.toUpperCase(),
                                              type: type as any,
                                              isEnabled: true,
                                              isLocked: false,
                                              x: 0.1, y: 0.1, w: 0.3, h: 0.3
                                            }
                                          ]
                                        };
                                      }
                                      return s;
                                    });
                                    setScenes(updated);
                                  }
                                }}
                                className="p-0.5 rounded bg-purple-950 hover:bg-purple-900 text-purple-300"
                              >
                                <Plus className="w-3.5 h-3.5" />
                              </button>
                            </div>
                            <div className="space-y-1">
                              {activeScene.sources.map(src => (
                                <div
                                  key={src.id}
                                  className="flex items-center justify-between bg-[#181429] px-2 py-1.5 rounded text-xs text-white"
                                >
                                  <span className="truncate">{src.name}</span>
                                  <div className="flex items-center gap-1.5 text-[#86809e]">
                                    <button
                                      onClick={() => {
                                        const updated = scenes.map(s => {
                                          if (s.id === activeSceneId) {
                                            return {
                                              ...s,
                                              sources: s.sources.map(it => it.id === src.id ? { ...it, isEnabled: !it.isEnabled } : it)
                                            };
                                          }
                                          return s;
                                        });
                                        setScenes(updated);
                                      }}
                                    >
                                      {src.isEnabled ? <Eye className="w-3 h-3 text-emerald-400" /> : <EyeOff className="w-3 h-3 text-red-400" />}
                                    </button>
                                    <button
                                      onClick={() => {
                                        const updated = scenes.map(s => {
                                          if (s.id === activeSceneId) {
                                            return {
                                              ...s,
                                              sources: s.sources.map(it => it.id === src.id ? { ...it, isLocked: !it.isLocked } : it)
                                            };
                                          }
                                          return s;
                                        });
                                        setScenes(updated);
                                      }}
                                    >
                                      {src.isLocked ? <Lock className="w-3 h-3 text-purple-400" /> : <Unlock className="w-3 h-3" />}
                                    </button>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                {/* Android Navigation Bar */}
                <div className="h-4 bg-[#0a0814] flex items-center justify-center shrink-0">
                  <div className="w-24 h-1 bg-[#3c3654] rounded-full" />
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: Android Project Code Explorer */}
        {activeTab === 'code' && (
          <div className="flex-1 flex overflow-hidden bg-[#090810]">
            {/* File Tree Sidebar */}
            <div className="w-80 border-r border-[#221c33] bg-[#0c0a17] flex flex-col shrink-0">
              <div className="p-3 border-b border-[#221c33] flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wider text-purple-300 font-mono">
                  Android Project Files
                </span>
                <span className="text-[10px] bg-[#1d1830] text-purple-400 px-1.5 py-0.5 rounded font-mono">
                  {ANDROID_PROJECT_FILES.length} Files
                </span>
              </div>
              <div className="flex-1 overflow-y-auto p-2 space-y-1">
                {ANDROID_PROJECT_FILES.map((file) => (
                  <button
                    key={file.path}
                    onClick={() => setSelectedFile(file)}
                    className={`w-full text-left p-2 rounded-lg text-xs flex flex-col gap-0.5 transition ${
                      selectedFile.path === file.path
                        ? 'bg-[#292247] border border-purple-500/50 text-white'
                        : 'text-[#9b93b5] hover:bg-[#161226] hover:text-white'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <FileCode className={`w-3.5 h-3.5 ${
                        file.category === 'build' ? 'text-amber-400' :
                        file.category === 'manifest' ? 'text-emerald-400' :
                        file.category === 'kotlin-recorder' ? 'text-purple-400' : 'text-blue-400'
                      }`} />
                      <span className="font-mono font-semibold truncate">{file.name}</span>
                    </div>
                    <span className="text-[10px] text-[#787191] truncate pl-5.5">
                      {file.description}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Code Content Viewer */}
            <div className="flex-1 flex flex-col overflow-hidden bg-[#0a0812]">
              {/* Code Header */}
              <div className="h-12 border-b border-[#221c33] px-4 flex items-center justify-between bg-[#0e0c1c]">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono font-bold text-purple-300">
                    android/{selectedFile.path}
                  </span>
                  <span className="text-[11px] text-[#797294] hidden sm:inline">
                    • {selectedFile.description}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={copyCodeToClipboard}
                    className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#201a36] hover:bg-[#2c244a] text-xs font-medium text-white border border-[#3b3259]"
                  >
                    {copiedCode ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedCode ? 'Copied' : 'Copy Code'}</span>
                  </button>
                </div>
              </div>

              {/* Code Body */}
              <div className="flex-1 overflow-auto p-4 font-mono text-xs text-[#cfcbe3] leading-relaxed bg-[#07050d]">
                <pre className="whitespace-pre">{selectedFile.content}</pre>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: Build & APK Guide */}
        {activeTab === 'guide' && (
          <div className="flex-1 overflow-y-auto p-6 max-w-4xl mx-auto space-y-6">
            <div>
              <h2 className="text-xl font-extrabold text-white tracking-wide">
                DARK ALISE OBS: Compilation, APK Generation & Testing Guide
              </h2>
              <p className="text-sm text-[#9f98be] mt-1">
                Follow these exact production steps to build and run the native application on your Samsung Galaxy A14 5G or any Android 10+ device.
              </p>
            </div>

            {/* Step 1: Requirements */}
            <div className="bg-[#120f21] border border-[#26203d] rounded-xl p-4 space-y-2">
              <div className="text-xs font-bold uppercase tracking-wider text-purple-400 font-mono">
                1. System Prerequisites
              </div>
              <ul className="text-xs text-[#c9c5de] space-y-1 list-disc list-inside">
                <li>JDK 17 or JDK 21 (Temurin / Zulu / OpenJDK)</li>
                <li>Android Studio Ladybug (2024.2+) or Android SDK CLI</li>
                <li>Android SDK Platform API 35 with Build-Tools 35.0.0</li>
                <li>USB Debugging enabled on Samsung Galaxy A14 5G (Settings → Developer Options)</li>
              </ul>
            </div>

            {/* Step 2: Build Commands */}
            <div className="bg-[#120f21] border border-[#26203d] rounded-xl p-4 space-y-3">
              <div className="text-xs font-bold uppercase tracking-wider text-purple-400 font-mono">
                2. Gradle APK Compilation
              </div>
              <p className="text-xs text-[#9f98be]">
                Extract the downloaded project ZIP and run the following in terminal:
              </p>
              <div className="bg-[#08070f] border border-[#221c36] p-3 rounded-lg font-mono text-xs text-purple-300 space-y-2">
                <div>
                  <span className="text-[#6d6687]"># Grant execute permission to Gradle wrapper (Linux/macOS):</span><br />
                  chmod +x gradlew
                </div>
                <div>
                  <span className="text-[#6d6687]"># Compile Debug APK:</span><br />
                  ./gradlew assembleDebug
                </div>
                <div>
                  <span className="text-[#6d6687]"># Compile Optimized Release APK with R8 Minification:</span><br />
                  ./gradlew assembleRelease
                </div>
              </div>
              <p className="text-xs text-[#9f98be]">
                Generated APK output: <code className="text-purple-300">app/build/outputs/apk/debug/app-debug.apk</code>
              </p>
            </div>

            {/* Step 3: Installation on Samsung Galaxy A14 5G */}
            <div className="bg-[#120f21] border border-[#26203d] rounded-xl p-4 space-y-3">
              <div className="text-xs font-bold uppercase tracking-wider text-purple-400 font-mono">
                3. Installation via ADB
              </div>
              <div className="bg-[#08070f] border border-[#221c36] p-3 rounded-lg font-mono text-xs text-purple-300 space-y-1">
                <div>adb install -r app/build/outputs/apk/debug/app-debug.apk</div>
                <div>adb shell am start -n com.darkalise.obs/.MainActivity</div>
              </div>
            </div>

            {/* Step 4: Hardware Tuning */}
            <div className="bg-[#120f21] border border-[#26203d] rounded-xl p-4 space-y-2">
              <div className="text-xs font-bold uppercase tracking-wider text-purple-400 font-mono">
                4. Samsung Galaxy A14 5G Optimization Notes
              </div>
              <ul className="text-xs text-[#c9c5de] space-y-1.5 list-disc list-inside">
                <li>
                  <strong>Hardware Encoder:</strong> Samsung Galaxy A14 5G features the MediaTek Dimensity 700 (or Exynos 1330 depending on market). The app prioritizes <code className="text-purple-300">OMX.MTK.video.encoder.avc</code> with automatic fallback to <code className="text-purple-300">c2.android.avc.encoder</code>.
                </li>
                <li>
                  <strong>Bitrate recommendation:</strong> 8 Mbps for 1080p 60 FPS yields smooth recording with minimal CPU thermal throttling.
                </li>
                <li>
                  <strong>Scoped Storage:</strong> Videos write directly into <code className="text-purple-300">Movies/DarkAliseOBS/</code> using MediaStore, accessible through Samsung Gallery or files app immediately.
                </li>
                <li>
                  <strong>Discord/VoIP Audio Restriction:</strong> Android enforces playback capture protection. Apps that set <code className="text-purple-300">USAGE_VOICE_COMMUNICATION</code> cannot be tapped without root; the app displays the security explanation accurately.
                </li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
