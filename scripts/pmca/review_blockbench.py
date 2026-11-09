"""Open editable sources in an isolated headless Blockbench Web browser.
Uses Playwright's virtual file chooser, never desktop input or a personal profile.
Run with Python containing playwright (the machine also has Python 3.13).
"""
from pathlib import Path
import asyncio,json,re,argparse
from playwright.async_api import async_playwright
R=Path(__file__).resolve().parents[2]
OUT=R/'reports/blockbench-review'
async def main(models=None,texture_size=64):
    OUT.mkdir(parents=True,exist_ok=True)
    console=[];errors=[];posts=[];results=[]
    async with async_playwright() as p:
        browser=await p.chromium.launch(headless=True)
        context=await browser.new_context(viewport={'width':1440,'height':960},device_scale_factor=1,locale='en-US',service_workers='block')
        async def only_get(route):
            if route.request.method=='POST':
                posts.append({'method':'POST','url':route.request.url,'blocked':True});await route.abort()
            else:await route.continue_()
        await context.route('**/*',only_get)
        page=await context.new_page()
        page.on('console',lambda m:console.append({'type':m.type,'text':m.text}))
        page.on('pageerror',lambda e:errors.append(str(e)))
        await page.goto('https://web.blockbench.net/',wait_until='domcontentloaded',timeout=60000)
        print('Blockbench document loaded',flush=True)
        try:
            await page.get_by_role('button',name=re.compile(r'^(Open Model|Abrir modelo)$',re.I)).wait_for(timeout=30000)
        except Exception:
            await page.screenshot(path=str(OUT/'startup-failure.png'))
            (OUT/'startup-diagnostic.json').write_text(json.dumps({'text':await page.locator('body').inner_text(),'errors':errors,'console':console},indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
            raise
        for name,relative,count in (models or [('tricorne','models/blockbench/head/tricorne_classic.bbmodel',15),('reference','models/blockbench/reference_adult_mca.bbmodel',6)]):
            if not results:
                async with page.expect_file_chooser() as chooser:
                    await page.get_by_role('button',name=re.compile(r'^(Open Model|Abrir modelo)$',re.I)).click()
                await (await chooser.value).set_files(str(R/relative))
                print('Selected local source:',relative,flush=True)
            else:
                async with page.expect_file_chooser() as chooser:
                    await page.keyboard.press('Control+o')
                await (await chooser.value).set_files(str(R/relative))
            await page.wait_for_function('(count) => typeof Cube !== "undefined" && Cube.all.length === count',arg=count,timeout=30000)
            await page.wait_for_function(f'Texture.all.length > 0 && Texture.all[0].img.complete && Texture.all[0].img.naturalWidth === {texture_size}',timeout=30000)
            await page.keyboard.press('Home')
            result=await page.evaluate('''() => ({version:Blockbench.version,name:Project.name,format:Format.id,cubes:Cube.all.map(c=>({name:c.name,from:c.from,to:c.to})),textures:Texture.all.map(t=>({name:t.name,width:t.img.naturalWidth,height:t.img.naturalHeight,error:t.error})),webgl:!!Preview.selected?.renderer?.getContext()})''')
            assert len(result['cubes'])==count,result
            assert all(t['width']==texture_size and t['height']==texture_size and not t['error'] for t in result['textures']),result
            await page.screenshot(path=str(OUT/(name+'-opened.png')))
            result.update(source=relative,status='PASS');results.append(result)
        await context.close();await browser.close()
    evidence={'isolated':True,'headless':True,'personal_profile_used':False,'results':results,'page_errors':errors,'post_requests':posts,'console':console}
    (OUT/'execution.json').write_text(json.dumps(evidence,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print(json.dumps({'status':'PASS','models':len(results),'version':results[0]['version'],'page_errors':errors,'post_requests':posts},ensure_ascii=False))
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--texture-size',type=int,default=64);parser.add_argument('--model');parser.add_argument('--count',type=int);parser.add_argument('--name',default='asset');parser.add_argument('--out');args=parser.parse_args()
    if args.out:OUT=R/args.out
    if args.model and not args.count:parser.error('--count required with --model')
    asyncio.run(main([(args.name,args.model,args.count)] if args.model else None,args.texture_size))
